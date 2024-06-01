/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ChestMenuQuickMoveEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.mc.event.ScreenInitEvent;
import com.wynntils.mc.event.ScreenOpenedEvent;
import com.wynntils.models.containers.containers.reward.LootChestContainer;
import com.wynntils.models.containers.containers.reward.RewardContainer;
import com.wynntils.models.containers.event.MythicFoundEvent;
import com.wynntils.models.containers.type.LootChestTier;
import com.wynntils.models.containers.type.MythicFind;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.items.game.EmeraldItem;
import com.wynntils.models.items.items.game.GearBoxItem;
import com.wynntils.models.items.items.game.GearItem;
import com.wynntils.services.mapdata.providers.builtin.LootChestsProvider;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.RangedValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class LootChestModel extends Model {
    private static final int LOOT_CHEST_ITEM_COUNT = 27;

    @Persisted
    private final Storage<List<LootChestsProvider.FoundChestLocation>> foundChestLocations =
            new Storage<>(new ArrayList<>());

    @Persisted
    private final Storage<List<MythicFind>> mythicFinds = new Storage<>(new ArrayList<>());

    @Persisted
    private final Storage<Integer> openedChestCount = new Storage<>(0);

    @Persisted
    private final Storage<Integer> dryCount = new Storage<>(0);

    @Persisted
    private final Storage<Integer> dryBoxes = new Storage<>(0);

    @Persisted
    private final Storage<Integer> dryEmeralds = new Storage<>(0);

    @Persisted
    private final Storage<Map<GearTier, Integer>> dryItemTiers = new Storage<>(new EnumMap<>(GearTier.class));

    private BlockPos lastChestPos;
    private int nextExpectedLootContainerId = -2;

    public LootChestModel() {
        super(List.of());
    }

    @Override
    public void onStorageLoad(Storage<?> storage) {
        if (storage == foundChestLocations) {
            Services.MapData.LOOT_CHESTS_PROVIDER.updateFoundChests(foundChestLocations.get());
        }
    }

    @SubscribeEvent
    public void onQuickMove(ChestMenuQuickMoveEvent event) {
        if (event.getContainerId() == nextExpectedLootContainerId) {
            nextExpectedLootContainerId = -2;
        }
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.InteractAt event) {
        Entity entity = event.getEntityHitResult().getEntity();
        if (entity != null && entity.getType() == EntityType.SLIME) {
            // We don't actually know if this is a chest, but it's a good enough guess.
            lastChestPos = entity.blockPosition();
        }
    }

    @SubscribeEvent
    public void onScreenInit(ScreenInitEvent e) {
        if (Models.Container.getCurrentContainer() instanceof RewardContainer rewardContainer) {
            nextExpectedLootContainerId = rewardContainer.getContainerId();

            openedChestCount.store(openedChestCount.get() + 1);
            dryCount.store(dryCount.get() + 1);
        } else {
            lastChestPos = null;
        }
    }

    @SubscribeEvent
    public void onSetSlot(ContainerSetSlotEvent.Post event) {
        if (event.getContainerId() != nextExpectedLootContainerId) return;
        if (event.getSlot() >= LOOT_CHEST_ITEM_COUNT) return;

        ItemStack itemStack = event.getItemStack();

        processItemFind(itemStack);

        Optional<GearBoxItem> gearBoxItem = Models.Item.asWynnItem(itemStack, GearBoxItem.class);
        if (gearBoxItem.isEmpty()) return;

        GearBoxItem gearBox = gearBoxItem.get();
        if (gearBox.getGearTier() == GearTier.MYTHIC) {
            WynntilsMod.postEvent(new MythicFoundEvent(itemStack, false));

            if (gearBox.getGearType() != GearType.MASTERY_TOME) {
                storeMythicFind(itemStack, gearBox.getLevelRange());
                resetNormalDryStatistics();
            }
        }
    }

    @SubscribeEvent
    public void onScreenOpened(ScreenOpenedEvent.Post event) {
        if (lastChestPos == null) return;
        if (!(event.getScreen() instanceof ContainerScreen)) return;

        if (!(Models.Container.getCurrentContainer() instanceof LootChestContainer)) {
            lastChestPos = null;
            return;
        }

        LootChestTier chestType = Models.LootChest.getChestType(event.getScreen());
        if (chestType == null) return;

        Location location = new Location(lastChestPos);

        if (foundChestLocations.get().stream()
                .noneMatch(foundLocation -> foundLocation.getLocation().equals(location))) {
            addFoundChestLocation(new LootChestsProvider.FoundChestLocation(location, chestType));

            // TODO: Replace this notification with a popup
            Managers.Notification.queueMessage(
                    Component.literal("Added new waypoint for " + chestType.getWaypointName())
                            .withStyle(ChatFormatting.AQUA));
        }
    }

    public int getDryCount() {
        return dryCount.get();
    }

    public int getDryBoxes() {
        return dryBoxes.get();
    }

    public int getOpenedChestCount() {
        return openedChestCount.get();
    }

    public List<MythicFind> getMythicFinds() {
        return Collections.unmodifiableList(mythicFinds.get());
    }

    public LootChestTier getChestType(Screen screen) {
        return LootChestTier.fromTitle(screen);
    }

    public List<LootChestsProvider.FoundChestLocation> getFoundChestLocations() {
        return Collections.unmodifiableList(foundChestLocations.get());
    }

    public void addFoundChestLocation(LootChestsProvider.FoundChestLocation location) {
        foundChestLocations.get().add(location);
        foundChestLocations.touched();
        Services.MapData.LOOT_CHESTS_PROVIDER.updateFoundChests(foundChestLocations.get());
    }

    public void removeFoundChestLocation(LootChestsProvider.FoundChestLocation location) {
        foundChestLocations.get().remove(location);
        foundChestLocations.touched();
        Services.MapData.LOOT_CHESTS_PROVIDER.updateFoundChests(foundChestLocations.get());
    }

    private void processItemFind(ItemStack itemStack) {
        Optional<EmeraldItem> emeraldOptional = Models.Item.asWynnItem(itemStack, EmeraldItem.class);
        if (emeraldOptional.isPresent()) {
            dryEmeralds.store(dryEmeralds.get() + emeraldOptional.get().getEmeraldValue());
            return;
        }

        Optional<GearBoxItem> gearBoxOptional = Models.Item.asWynnItem(itemStack, GearBoxItem.class);
        if (gearBoxOptional.isPresent()) {
            GearTier gearBoxTier = gearBoxOptional.get().getGearTier();

            if (gearBoxTier == GearTier.MYTHIC) {
                // we don't store the actual "MYTHIC" in the dry data
                return;
            }

            dryBoxes.store(dryBoxes.get() + 1);
            dryItemTiers.get().merge(gearBoxTier, 1, Integer::sum);
            dryItemTiers.touched();
            return;
        }

        Optional<GearItem> gearOptional = Models.Item.asWynnItem(itemStack, GearItem.class);
        if (gearOptional.isPresent()) {
            // Technically we can only find identified Normal tier gear, but we'll check anyway
            GearTier gearTier = gearOptional.get().getGearTier();
            dryItemTiers.get().merge(gearTier, 1, Integer::sum);
            dryItemTiers.touched();
            return;
        }
    }

    private void storeMythicFind(ItemStack itemStack, RangedValue levelRange) {
        mythicFinds
                .get()
                .add(new MythicFind(
                        StyledText.fromComponent(itemStack.getHoverName()).getStringWithoutFormatting(),
                        levelRange,
                        openedChestCount.get(),
                        dryCount.get(),
                        dryBoxes.get(),
                        dryEmeralds.get(),
                        dryItemTiers.get(),
                        new Location(lastChestPos),
                        System.currentTimeMillis()));

        mythicFinds.touched();
    }

    private void resetNormalDryStatistics() {
        dryBoxes.store(0);
        dryCount.store(0);
        dryEmeralds.store(0);
        dryItemTiers.store(new EnumMap<>(GearTier.class));
    }
}
