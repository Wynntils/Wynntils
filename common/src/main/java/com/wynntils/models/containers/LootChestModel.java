/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.storage.Storage;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ChestMenuQuickMoveEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.models.containers.event.MythicFoundEvent;
import com.wynntils.models.containers.type.MythicFind;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.items.game.GearBoxItem;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.wynn.WynnItemMatchers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class LootChestModel extends Model {
    private static final int LOOT_CHEST_ITEM_COUNT = 27;

    private final Storage<List<MythicFind>> mythicFinds = new Storage<>(new ArrayList<>());
    private final Storage<Integer> openedChestCount = new Storage<>(0);
    private final Storage<Integer> dryCount = new Storage<>(0);
    private final Storage<Integer> dryBoxes = new Storage<>(0);
    private Storage<Integer> dryEmeralds = new Storage<>(0);
    private Storage<EnumMap<GearTier, Integer>> dryItemTiers = new Storage<>(new EnumMap<>(GearTier.class));

    private BlockPos lastChestPos;
    private int nextExpectedLootContainerId = -2;

    public LootChestModel(ContainerModel containerModel) {
        super(List.of(containerModel));
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

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.InteractAt event) {
        Entity entity = event.getEntityHitResult().getEntity();
        if (entity != null && entity.getType() == EntityType.SLIME) {
            // We don't actually know if this is a chest, but it's a good enough guess.
            lastChestPos = entity.blockPosition();
        }
    }

    @SubscribeEvent
    public void onMenuOpened(MenuEvent.MenuOpenedEvent event) {
        if (Models.Container.isLootOrRewardChest(
                StyledText.fromComponent(event.getTitle()).getStringWithoutFormatting())) {
            nextExpectedLootContainerId = event.getContainerId();

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

        progressItemFound(event, lastChestPos);

        ItemStack itemStack = event.getItemStack();
        Optional<GearBoxItem> wynnItem = Models.Item.asWynnItem(itemStack, GearBoxItem.class);
        if (wynnItem.isEmpty()) return;
        GearBoxItem gearBox = wynnItem.get();
        if (gearBox.getGearTier() == GearTier.MYTHIC) {
            WynntilsMod.postEvent(new MythicFoundEvent(itemStack));
            if (gearBox.getGearType() != GearType.MASTERY_TOME) {
                mythicFinds
                        .get()
                        .add(new MythicFind(
                                StyledText.fromComponent(itemStack.getHoverName())
                                        .getStringWithoutFormatting(),
                                openedChestCount.get(),
                                dryCount.get(),
                                dryBoxes.get(),
                                dryEmeralds.get(),
                                dryItemTiers.get(),
                                System.currentTimeMillis(),
                                new Location(lastChestPos)));
                mythicFinds.touched();

                dryBoxes.store(0);
                dryCount.store(0);
                dryEmeralds.store(0);
                dryItemTiers.store(new EnumMap<>(GearTier.class));
            }
        } else {
            dryBoxes.store(dryBoxes.get() + 1);
        }
    }

    private void progressItemFound(ContainerSetSlotEvent event, BlockPos chestPos) {
        ItemStack itemStack = event.getItemStack();
        if (itemStack.is(Items.EMERALD)) {
            dryEmeralds.store(dryEmeralds.get() + itemStack.getCount());
        }
        if (WynnItemMatchers.isGearBox(itemStack)) {
            GearTier gearBoxTier = GearTier.fromComponent(itemStack.getHoverName());
            if (gearBoxTier == GearTier.MYTHIC) {
                // we don't store the actual "MYTHIC" in the dry data
                return;
            }
            dryItemTiers.get().merge(gearBoxTier, 1, Integer::sum);
            dryItemTiers.touched();
        }
        if (WynnItemMatchers.isGear(itemStack)) {
            dryItemTiers.get().merge(GearTier.NORMAL, 1, Integer::sum);
            dryItemTiers.touched();
        }
    }

    @SubscribeEvent
    public void onQuickMove(ChestMenuQuickMoveEvent event) {
        if (event.getContainerId() == nextExpectedLootContainerId) {
            nextExpectedLootContainerId = -2;
        }
    }
}
