/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.event.TextDisplayChangedEvent;
import com.wynntils.mc.event.AddEntityEvent;
import com.wynntils.mc.event.RemoveEntitiesEvent;
import com.wynntils.mc.event.SetEntityDataEvent;
import com.wynntils.models.abilities.event.TotemEvent;
import com.wynntils.models.abilities.type.ShamanTotem;
import com.wynntils.models.character.event.CharacterUpdateEvent;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.spells.event.SpellEvent;
import com.wynntils.models.spells.type.SpellType;
import com.wynntils.utils.mc.McUtils;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.neoforged.bus.api.SubscribeEvent;

public final class ShamanTotemModel extends Model {
    // Test in ShamanTotemModel_SHAMAN_TOTEM_TIMER_PATTERN
    private static final Pattern SHAMAN_TOTEM_TIMER = Pattern.compile(
            "§b(?<username>.+)'(?:s)? §7Totem\n(§c\\+(?<regen>\\d+)❤§7/s )?(§e\uE013 §7(\\d+)s )?§d\uE01F §7(?<time>\\d+)s");
    private static final int MAX_TOTEM_COUNT = 4;
    private static final double TOTEM_SEARCH_RADIUS = 5;
    private static final double TOTEM_VERTICAL_SEARCH_RADIUS = 10;
    private static final int CAST_MAX_DELAY_MS = 1000;
    private static final int DISPLAY_CONFIRM_MAX_DELAY_MS = 2000;
    private static final float SHAMAN_TOTEM_CUSTOM_MODEL_DATA = 30601.0f;
    // TODO: CAST_MAX_DELAY could be a config when model configs eventually exist
    // it kind of depends on ping and server lag

    private final ShamanTotem[] totems = new ShamanTotem[MAX_TOTEM_COUNT]; // 0-indexed list of totems 1-4
    private final Map<Integer, Integer> pendingVisibleEntityIds = new HashMap<>(); // entity id -> totem number
    private final Map<Integer, PositionSnapshot> recentItemDisplaySpawns = new HashMap<>();
    private final Deque<PositionSnapshot> recentCastSnapshots = new ArrayDeque<>();
    private final Map<Integer, Integer> orphanedTimers =
            new HashMap<>(); // IDs of timers that can't really find a totem
    // These orphaned timers will be checked at a lower rate, they are probably timers that aren't actually totems
    private int nextTotemSlot = 1;

    public ShamanTotemModel() {
        super(List.of());
    }

    @SubscribeEvent
    public void onTotemSpellCast(SpellEvent.Cast e) {
        if (e.getSpellType() != SpellType.TOTEM) return;
        pruneOldCastSnapshots();
        pruneOldItemDisplaySpawns();
        // Wynncraft spawns the totem from the player's cast x/z, so keep a short-lived snapshot
        // instead of comparing against the player's position later after they may have moved.
        recentCastSnapshots.addLast(
                new PositionSnapshot(McUtils.player().getX(), McUtils.player().getZ(), System.currentTimeMillis()));
        pruneMissingPendingDisplays();
    }

    @SubscribeEvent
    public void onItemDisplaySpawn(AddEntityEvent e) {
        if (!Models.WorldState.onWorld()) return;
        if (Models.Character.getClassType() != ClassType.SHAMAN) return;
        if (!(e.getEntity() instanceof Display.ItemDisplay)) return;

        pruneOldItemDisplaySpawns();
        // Capture the raw spawn x/z before the display starts moving; the later item-data packet can
        // arrive after the visual totem has already drifted away from its spawn point.
        recentItemDisplaySpawns.put(e.getId(), new PositionSnapshot(e.getX(), e.getZ(), System.currentTimeMillis()));
    }

    @SubscribeEvent
    public void onTotemItemDisplayData(SetEntityDataEvent e) {
        if (!Models.WorldState.onWorld()) return;
        if (Models.Character.getClassType() != ClassType.SHAMAN) return;

        Entity entity = McUtils.mc().level.getEntity(e.getId());
        if (!(entity instanceof Display.ItemDisplay totemDisplay)) return;
        if (!wasLikelyCreatedByRecentCast(recentItemDisplaySpawns.get(e.getId()), totemDisplay.position())) return;
        if (isTrackedVisibleEntityId(totemDisplay.getId())) return;

        ItemStack data = getUpdatedItemStack(e);
        if (!isShamanTotemItem(data)) return;
        registerPendingTotem(totemDisplay);
        recentItemDisplaySpawns.remove(totemDisplay.getId());
    }

    @SubscribeEvent
    public void onTimerRename(TextDisplayChangedEvent.Text event) {
        if (!Models.WorldState.onWorld()) return;
        if (Models.Character.getClassType() != ClassType.SHAMAN) return;

        Display.TextDisplay textDisplay = event.getTextDisplay();

        StyledText name = event.getText();
        if (name.isEmpty()) return;
        Matcher m = name.getMatcher(SHAMAN_TOTEM_TIMER);
        if (!m.matches()) return;
        if (!(m.group("username").equals(McUtils.playerName()))) return;

        int parsedTime = Integer.parseInt(m.group("time"));
        int timerId = textDisplay.getId();

        ShamanTotem boundTotem = getBoundTotem(timerId);
        if (boundTotem != null) {
            updateTotem(boundTotem, parsedTime, textDisplay);
            return;
        }

        Integer matchedTotemNumber = findMatchingPendingTotem(textDisplay);
        if (matchedTotemNumber == null) {
            orphanedTimers.merge(timerId, 1, Integer::sum);
            if (orphanedTimers.get(timerId) == 2) {
                WynntilsMod.warn(
                        "Matched an unbound totem timer " + timerId + " but couldn't find a totem to bind it to.");
            }
            return;
        }

        bindPendingTotem(matchedTotemNumber, timerId, parsedTime, textDisplay);
    }

    @SubscribeEvent
    public void onTotemDestroy(RemoveEntitiesEvent e) {
        if (!Models.WorldState.onWorld()) return;

        List<Integer> destroyedEntities = e.getEntityIds();
        destroyedEntities.forEach(pendingVisibleEntityIds::remove);
        destroyedEntities.forEach(recentItemDisplaySpawns::remove);

        for (ShamanTotem totem : totems) {
            if (totem != null
                    && (destroyedEntities.contains(totem.getTimerEntityId())
                            || destroyedEntities.contains(totem.getVisibleEntityId()))) {
                removeTotem(totem.getTotemNumber());
            }
        }
    }

    @SubscribeEvent
    public void onClassChange(CharacterUpdateEvent e) {
        removeAllTotems();
    }

    private void registerPendingTotem(Display.ItemDisplay totemDisplay) {
        int totemNumber = getNextTotemSlot();
        if (totems[totemNumber - 1] != null) {
            removeTotem(totemNumber);
        }

        ShamanTotem newTotem = new ShamanTotem(
                totemNumber, -1, totemDisplay.getId(), -1, ShamanTotem.TotemState.SUMMONED, totemDisplay.position());

        totems[totemNumber - 1] = newTotem;
        pendingVisibleEntityIds.put(totemDisplay.getId(), totemNumber);
        WynntilsMod.postEvent(new TotemEvent.Summoned(totemNumber, totemDisplay));
    }

    private Integer findMatchingPendingTotem(Display.TextDisplay textDisplay) {
        pruneMissingPendingDisplays();

        // Prefer explicit entity relationships first, then fall back to spatial matching.
        Entity vehicle = textDisplay.getVehicle();
        if (vehicle instanceof Display.ItemDisplay mountedDisplay) {
            Integer mountedTotemNumber = pendingVisibleEntityIds.get(mountedDisplay.getId());
            if (mountedTotemNumber != null) {
                return mountedTotemNumber;
            }
        }

        Integer passengerMatch = findPassengerLinkedTotem(textDisplay);
        if (passengerMatch != null) {
            return passengerMatch;
        }

        return findNearestPendingTotem(textDisplay.position());
    }

    private Integer findPassengerLinkedTotem(Display.TextDisplay textDisplay) {
        for (Map.Entry<Integer, Integer> entry : pendingVisibleEntityIds.entrySet()) {
            Entity entity = McUtils.mc().level.getEntity(entry.getKey());
            if (!(entity instanceof Display.ItemDisplay itemDisplay)) continue;

            if (itemDisplay.getPassengers().contains(textDisplay)) {
                return entry.getValue();
            }
        }

        return null;
    }

    private Integer findNearestPendingTotem(Position textPosition) {
        Integer bestTotemNumber = null;
        double bestScore = Double.MAX_VALUE;

        for (Map.Entry<Integer, Integer> entry : pendingVisibleEntityIds.entrySet()) {
            Entity entity = McUtils.mc().level.getEntity(entry.getKey());
            if (!(entity instanceof Display.ItemDisplay itemDisplay)) continue;
            if (!isWithinSearchBounds(textPosition, itemDisplay.position())) continue;

            double score = matchScore(textPosition, itemDisplay.position());
            if (score >= bestScore) continue;

            bestScore = score;
            bestTotemNumber = entry.getValue();
        }

        return bestTotemNumber;
    }

    private void bindPendingTotem(int totemNumber, int timerId, int parsedTime, Display.TextDisplay textDisplay) {
        ShamanTotem totem = getTotem(totemNumber);
        if (totem == null) return;

        Display.ItemDisplay visibleDisplay = getVisibleDisplay(totem.getVisibleEntityId());
        Position boundPosition = visibleDisplay != null ? visibleDisplay.position() : textDisplay.position();

        totem.setTimerEntityId(timerId);
        totem.setTime(parsedTime);
        totem.setPosition(boundPosition);
        totem.setState(ShamanTotem.TotemState.ACTIVE);

        pendingVisibleEntityIds.remove(totem.getVisibleEntityId());
        if (orphanedTimers.containsKey(timerId) && orphanedTimers.get(timerId) > 1) {
            WynntilsMod.info("Matched an orphaned totem timer " + timerId + " to a totem " + totem.getTotemNumber()
                    + " after " + orphanedTimers.get(timerId) + " attempts.");
        }
        orphanedTimers.remove(timerId);

        WynntilsMod.postEvent(new TotemEvent.Activated(totem.getTotemNumber(), boundPosition));
    }

    private void updateTotem(ShamanTotem totem, int parsedTime, Display.TextDisplay textDisplay) {
        Display.ItemDisplay visibleDisplay = getVisibleDisplay(totem.getVisibleEntityId());
        Position updatedPosition = visibleDisplay != null ? visibleDisplay.position() : textDisplay.position();

        totem.setTime(parsedTime);
        totem.setPosition(updatedPosition);

        WynntilsMod.postEvent(new TotemEvent.Updated(totem.getTotemNumber(), parsedTime, updatedPosition));
    }

    private boolean wasLikelyCreatedByRecentCast(PositionSnapshot spawnSnapshot, Position displayPosition) {
        if (Models.Inventory.hasAutoCasterItem()) return true;

        pruneOldCastSnapshots();
        // Use the cached spawn x/z when available, since SetEntityDataEvent can arrive after the
        // display has already fallen or shifted away from the actual cast location.
        double displayX = spawnSnapshot != null ? spawnSnapshot.x() : displayPosition.x();
        double displayZ = spawnSnapshot != null ? spawnSnapshot.z() : displayPosition.z();
        for (PositionSnapshot castSnapshot : recentCastSnapshots) {
            if (Math.abs(displayX - castSnapshot.x()) <= TOTEM_SEARCH_RADIUS
                    && Math.abs(displayZ - castSnapshot.z()) <= TOTEM_SEARCH_RADIUS) {
                return true;
            }
        }

        return false;
    }

    private boolean isShamanTotemItem(ItemStack itemStack) {
        if (!itemStack.is(Items.OAK_BOAT)) return false;
        CustomModelData customModelData = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (customModelData == null) return false;

        return customModelData.floats().stream().anyMatch(model -> model == SHAMAN_TOTEM_CUSTOM_MODEL_DATA);
    }

    private ItemStack getUpdatedItemStack(SetEntityDataEvent event) {
        for (SynchedEntityData.DataValue<?> dataValue : event.getPackedItems()) {
            if (dataValue.id() != Display.ItemDisplay.DATA_ITEM_STACK_ID.id()) continue;
            if (dataValue.value() instanceof ItemStack itemStack) {
                return itemStack;
            }
        }

        return ItemStack.EMPTY;
    }

    private void pruneMissingPendingDisplays() {
        pendingVisibleEntityIds.entrySet().removeIf(entry -> {
            Entity entity = McUtils.mc().level.getEntity(entry.getKey());
            return !(entity instanceof Display.ItemDisplay);
        });
    }

    private void pruneOldItemDisplaySpawns() {
        long now = System.currentTimeMillis();
        recentItemDisplaySpawns
                .entrySet()
                .removeIf(entry -> now - entry.getValue().timestamp() > DISPLAY_CONFIRM_MAX_DELAY_MS);
    }

    private void pruneOldCastSnapshots() {
        long now = System.currentTimeMillis();
        while (!recentCastSnapshots.isEmpty()
                && now - recentCastSnapshots.peekFirst().timestamp() > CAST_MAX_DELAY_MS) {
            recentCastSnapshots.removeFirst();
        }
    }

    private boolean isTrackedVisibleEntityId(int entityId) {
        return pendingVisibleEntityIds.containsKey(entityId) || getTotemByVisibleEntityId(entityId) != null;
    }

    /**
     * Removes the given totem from the list of totems.
     * @param totem The totem to remove. Must be 1, 2, 3 or 4
     */
    private void removeTotem(int totem) {
        ShamanTotem existingTotem = totems[totem - 1];
        if (existingTotem == null) return;

        WynntilsMod.postEvent(new TotemEvent.Removed(totem, existingTotem));
        pendingVisibleEntityIds.remove(existingTotem.getVisibleEntityId());
        totems[totem - 1] = null;
        nextTotemSlot = totem;
    }

    /**
     * Resets all three totem variables.
     */
    private void removeAllTotems() {
        for (int i = 1; i <= MAX_TOTEM_COUNT; i++) {
            removeTotem(i);
        }

        recentCastSnapshots.clear();
        recentItemDisplaySpawns.clear();
        pendingVisibleEntityIds.clear();
        orphanedTimers.clear();
        nextTotemSlot = 1;
    }

    private int getNextTotemSlot() {
        for (int i = 0; i < MAX_TOTEM_COUNT; i++) {
            if (totems[i] == null) {
                nextTotemSlot = i + 1;
                return nextTotemSlot;
            }
        }

        int toReturn = nextTotemSlot;

        if (nextTotemSlot == MAX_TOTEM_COUNT) {
            nextTotemSlot = 1;
        } else {
            nextTotemSlot += 1;
        }

        return toReturn;
    }

    /**
     * Gets the totem bound to the given timerId.
     * @param timerId The timerId that is checked against the totems
     * @return The totem bound to the given timerId, or null if no totem is bound
     */
    private ShamanTotem getBoundTotem(int timerId) {
        for (ShamanTotem totem : totems) {
            if (totem != null && totem.getTimerEntityId() == timerId) {
                return totem;
            }
        }

        return null;
    }

    private ShamanTotem getTotemByVisibleEntityId(int visibleEntityId) {
        for (ShamanTotem totem : totems) {
            if (totem != null && totem.getVisibleEntityId() == visibleEntityId) {
                return totem;
            }
        }

        return null;
    }

    private Display.ItemDisplay getVisibleDisplay(int visibleEntityId) {
        Entity entity = McUtils.mc().level.getEntity(visibleEntityId);
        return entity instanceof Display.ItemDisplay itemDisplay ? itemDisplay : null;
    }

    private boolean isWithinSearchBounds(Position textPosition, Position displayPosition) {
        double deltaX = Math.abs(textPosition.x() - displayPosition.x());
        double deltaY = textPosition.y() - displayPosition.y();
        double deltaZ = Math.abs(textPosition.z() - displayPosition.z());

        return deltaX <= TOTEM_SEARCH_RADIUS
                && deltaZ <= TOTEM_SEARCH_RADIUS
                && deltaY >= -1
                && deltaY <= TOTEM_VERTICAL_SEARCH_RADIUS;
    }

    private double matchScore(Position textPosition, Position displayPosition) {
        double deltaX = textPosition.x() - displayPosition.x();
        double deltaZ = textPosition.z() - displayPosition.z();
        double verticalOffset = Math.max(0, textPosition.y() - displayPosition.y());

        double horizontalDistanceSquared = deltaX * deltaX + deltaZ * deltaZ;
        return horizontalDistanceSquared * 4 + verticalOffset * verticalOffset;
    }

    private boolean isClose(Position pos1, Position pos2) {
        LocalPlayer player = McUtils.player();
        double dX = player.getX() - player.xOld;
        double dZ = player.getZ() - player.zOld;
        double dY = player.getY() - player.yOld;
        double speedMultiplier = Math.sqrt((dX * dX) + (dZ * dZ) + (dY * dY)) * 20;
        // wynn never casts perfectly aligned totems
        speedMultiplier = Math.max(speedMultiplier, 1);

        return Math.abs(pos1.x() - pos2.x()) < speedMultiplier
                && Math.abs(pos1.y() - pos2.y()) < speedMultiplier
                && Math.abs(pos1.z() - pos2.z()) < speedMultiplier;
    }

    public List<ShamanTotem> getActiveTotems() {
        return Arrays.stream(totems).filter(Objects::nonNull).toList();
    }

    public ShamanTotem getTotem(int totemNumber) {
        if (totemNumber < 1 || totemNumber > MAX_TOTEM_COUNT) {
            return null;
        }

        return totems[totemNumber - 1];
    }

    private record PositionSnapshot(double x, double z, long timestamp) {}
}
