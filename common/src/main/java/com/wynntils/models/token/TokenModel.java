/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.token;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.handlers.labels.event.EntityLabelChangedEvent;
import com.wynntils.handlers.labels.event.EntityLabelVisibilityEvent;
import com.wynntils.mc.event.RemoveEntitiesEvent;
import com.wynntils.models.containers.type.InventoryWatcher;
import com.wynntils.models.items.items.game.MiscItem;
import com.wynntils.models.token.event.TokenGatekeeperEvent;
import com.wynntils.models.token.type.TokenGatekeeper;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.TimedSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.core.Position;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TokenModel extends Model {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("^§a(\\d+)§2/(\\d+)$");
    private static final Pattern TYPE_PATTERN = Pattern.compile("^§7Get §e\\[\\d+ (.*)\\]$");
    private static final String VERIFICATION_STRING = "§7Right-click to add";

    private final Map<Integer, TokenGatekeeper> activeGatekeepers = new HashMap<>();
    private final Map<TokenGatekeeper, TokenInventoryWatcher> inventoryWatchers = new HashMap<>();
    private final TimedSet<BakingTokenGatekeeper> bakingGatekeepers = new TimedSet<>(5, TimeUnit.SECONDS, true);
    private final Map<Integer, TokenGatekeeper> invisibleGatekeepers = new HashMap<>();

    public TokenModel() {
        super(List.of());
    }

    public Stream<TokenGatekeeper> getGatekeepers() {
        return activeGatekeepers.values().stream();
    }

    public CappedValue getCollected(TokenGatekeeper gatekeeper) {
        TokenInventoryWatcher watcher = inventoryWatchers.get(gatekeeper);
        // The null case should not happen, but just to be safe
        int inventoryCount = watcher != null ? watcher.getTotalCount() : 0;

        CappedValue deposited = gatekeeper.getDeposited();
        int total = Math.min(deposited.current() + inventoryCount, deposited.max());
        return new CappedValue(total, deposited.max());
    }

    @SubscribeEvent
    public void onLabelChange(EntityLabelChangedEvent event) {
        if (!(event.getEntity() instanceof ArmorStand)) return;

        String name = event.getName();

        Matcher typeMatcher = TYPE_PATTERN.matcher(name);
        if (typeMatcher.matches()) {
            String type = typeMatcher.group(1);

            BakingTokenGatekeeper baking = getBaking(event.getEntity().position());
            baking.type = type;
            checkAndPromoteBaking();
            return;
        }

        Matcher tokensMatcher = TOKEN_PATTERN.matcher(name);
        if (tokensMatcher.matches()) {
            CappedValue tokens =
                    new CappedValue(Integer.parseInt(tokensMatcher.group(1)), Integer.parseInt(tokensMatcher.group(2)));

            int id = event.getEntity().getId();
            TokenGatekeeper gatekeeper = activeGatekeepers.get(id);
            if (gatekeeper != null) {
                // Update active gatekeeper
                gatekeeper.setDeposited(tokens);
                WynntilsMod.postEvent(new TokenGatekeeperEvent.Deposited(gatekeeper));
                return;
            }

            TokenGatekeeper invisibleGatekeeper = invisibleGatekeepers.get(id);
            if (invisibleGatekeeper != null) {
                // We need to keep invisible gatekeepers up to date if they suddenly become visible
                invisibleGatekeeper.setDeposited(tokens);
                return;
            }

            // Create new baking gatekeeper
            BakingTokenGatekeeper baking = getBaking(event.getEntity().position());
            baking.value = tokens;
            baking.valueEntityId = event.getEntity().getId();
            checkAndPromoteBaking();
            return;
        }

        // This does not provide any new information for the TokenGatekeeper, but it is a good
        // extra check that we really match only on real gatekeepers
        if (name.equals(VERIFICATION_STRING)) {
            BakingTokenGatekeeper baking = getBaking(event.getEntity().position());
            baking.confirmed = true;
            checkAndPromoteBaking();
        }
    }

    @SubscribeEvent
    public void onLabelVisibility(EntityLabelVisibilityEvent event) {
        if (!event.getVisibility()) {
            // This is the normal way in which gatekeepers are "removed" when done
            int id = event.getEntity().getId();
            TokenGatekeeper gatekeeper = activeGatekeepers.get(id);
            if (gatekeeper == null) return;

            removeGatekeeper(id, gatekeeper);
            invisibleGatekeepers.put(id, gatekeeper);
        } else {
            // It became visible again; restore it
            int id = event.getEntity().getId();
            TokenGatekeeper gatekeeper = invisibleGatekeepers.get(id);
            if (gatekeeper == null) return;

            invisibleGatekeepers.remove(id);
            addGatekeeper(id, gatekeeper);
        }
    }

    // This happens if we log out, switch class, etc
    @SubscribeEvent
    public void onEntitiesRemoved(RemoveEntitiesEvent event) {
        for (int id : event.getEntityIds()) {
            if (activeGatekeepers.containsKey(id)) {
                removeGatekeeper(id, activeGatekeepers.get(id));
            }
            if (invisibleGatekeepers.containsKey(id)) {
                invisibleGatekeepers.remove(id);
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldStateEvent event) {
        activeGatekeepers.forEach(
                (id, gatekeeper) -> WynntilsMod.postEvent(new TokenGatekeeperEvent.Removed(gatekeeper)));
        inventoryWatchers.values().forEach(Models.PlayerInventory::unregisterWatcher);

        activeGatekeepers.clear();
        invisibleGatekeepers.clear();
        bakingGatekeepers.clear();
        inventoryWatchers.clear();
    }

    private void addGatekeeper(int entityId, TokenGatekeeper gatekeeper) {
        TokenInventoryWatcher watcher = new TokenInventoryWatcher(gatekeeper);
        inventoryWatchers.put(gatekeeper, watcher);
        Models.PlayerInventory.registerWatcher(watcher);
        activeGatekeepers.put(entityId, gatekeeper);

        WynntilsMod.postEvent(new TokenGatekeeperEvent.Added(gatekeeper));
    }

    private void removeGatekeeper(int entityId, TokenGatekeeper gatekeeper) {
        activeGatekeepers.remove(entityId);
        InventoryWatcher watcher = inventoryWatchers.get(gatekeeper);
        Models.PlayerInventory.unregisterWatcher(watcher);
        inventoryWatchers.remove(gatekeeper);

        WynntilsMod.postEvent(new TokenGatekeeperEvent.Removed(gatekeeper));
    }

    private BakingTokenGatekeeper getBaking(Position position) {
        for (BakingTokenGatekeeper baking : bakingGatekeepers) {
            if (baking.position.x() == position.x()
                    && baking.position.z() == position.z()
                    && Math.abs(baking.position.y() - position.y()) < 1.5) {
                return baking;
            }
        }
        BakingTokenGatekeeper baking = new BakingTokenGatekeeper(position);
        bakingGatekeepers.put(baking);
        return baking;
    }

    private void checkAndPromoteBaking() {
        Iterator<BakingTokenGatekeeper> iter = bakingGatekeepers.iterator();
        while (iter.hasNext()) {
            BakingTokenGatekeeper baking = iter.next();
            if (baking.confirmed && baking.type != null && baking.value != null) {
                iter.remove();

                addGatekeeper(baking.valueEntityId, baking.toGatekeeper());
            }
        }
    }

    private static final class BakingTokenGatekeeper {
        private final Position position;
        private String type;
        private CappedValue value;
        private int valueEntityId;
        private boolean confirmed;

        private BakingTokenGatekeeper(Position position) {
            this.position = position;
        }

        protected TokenGatekeeper toGatekeeper() {
            Location location = Location.containing(position).offset(0, 3, 0);
            return new TokenGatekeeper(type, location, value);
        }
    }

    protected static class TokenInventoryWatcher extends InventoryWatcher {
        private final TokenGatekeeper gatekeeper;

        protected TokenInventoryWatcher(TokenGatekeeper gatekeeper, String type) {
            super(itemStack -> isToken(type, itemStack));
            this.gatekeeper = gatekeeper;
        }

        protected TokenInventoryWatcher(TokenGatekeeper gatekeeper) {
            // Remove the trailing plural 's'
            this(gatekeeper, gatekeeper.getType().replaceAll("s$", ""));
        }

        private static boolean isToken(String type, ItemStack itemStack) {
            Optional<MiscItem> miscItemOpt = Models.Item.asWynnItem(itemStack, MiscItem.class);
            if (miscItemOpt.isEmpty()) return false;

            MiscItem miscItem = miscItemOpt.get();
            if (!miscItem.isUntradable()) return false;

            return (miscItem.getName().contains(type));
        }

        @Override
        protected void onUpdate(int oldSlots, int oldTotalCount) {
            WynntilsMod.postEvent(
                    new TokenGatekeeperEvent.InventoryUpdated(gatekeeper, this.getTotalCount(), oldTotalCount));
        }
    }
}
