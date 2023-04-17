/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrunstats;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.storage.Storage;
import com.wynntils.mc.event.ChestMenuQuickMoveEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.models.containers.ContainerModel;
import com.wynntils.models.containers.event.MythicFoundEvent;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.WynnItemMatchers;

import java.util.EnumMap;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class LootChestModel extends Model {
    private static final int LOOT_CHEST_ITEM_COUNT = 27;

    private Storage<Integer> totalCount = new Storage<>(0);
    private Storage<Integer> dryCount = new Storage<>(0);
    private Storage<Integer> dryBoxes = new Storage<>(0);
    private Storage<Integer> dryEmeralds = new Storage<>(0);
    private Storage<EnumMap<GearTier, Integer>> dryItemTiers = new Storage<>(new EnumMap<>(GearTier.class));

    private int nextExpectedLootContainerId = -2;
    private BlockPos lastChestPos = null;

    public LootChestModel(ContainerModel containerModel) {
        super(List.of(containerModel));
    }

    public int getTotalCount() {
        return totalCount.get();
    }

    public int getDryCount() {
        return dryCount.get();
    }

    public Integer getDryBoxes() {
        return dryBoxes.get();
    }

    @SubscribeEvent
    public void onChestClicked(PlayerInteractEvent.RightClickBlock event) {
        BlockEntity blockEntity = McUtils.mc().level.getBlockEntity(event.getPos());
        if (blockEntity == null || blockEntity.getType() != BlockEntityType.CHEST) {
            return;
        }
        lastChestPos = event.getPos();
    }

    @SubscribeEvent
    public void onMenuOpened(MenuEvent.MenuOpenedEvent event) {
        if (Models.Container.isLootChest(ComponentUtils.getUnformatted(event.getTitle()))) {
            nextExpectedLootContainerId = event.getContainerId();

            totalCount.store(totalCount.get() + 1);
            dryCount.store(dryCount.get() + 1);
            Managers.Config.saveConfig();
        }
    }

    @SubscribeEvent
    public void onSetSlot(ContainerSetSlotEvent event) {
        if (event.getContainerId() != nextExpectedLootContainerId) return;
        if (event.getSlot() >= LOOT_CHEST_ITEM_COUNT) return;

        progressItemFound(event, lastChestPos);

        ItemStack itemStack = event.getItemStack();

        if (!WynnItemMatchers.isGearBox(itemStack)) return;

        GearTier gearTier = GearTier.fromComponent(itemStack.getHoverName());

        if (gearTier == GearTier.MYTHIC) {
            // TODO: 16/04/2023 store mythic and dry stats to mythic
            dryBoxes.store(0);
            dryCount.store(0);
            dryEmeralds.store(0);
            dryItemTiers.store(new EnumMap<>(GearTier.class));
            WynntilsMod.postEvent(new MythicFoundEvent(itemStack));
        } else {
            dryBoxes.store(dryBoxes.get() + 1);
        }

        Managers.Config.saveConfig();
    }

    private void progressItemFound(ContainerSetSlotEvent event, BlockPos chestPos) {
        ItemStack itemStack = event.getItemStack();
        if (itemStack.is(Items.EMERALD)) {
            dryEmeralds.store(dryEmeralds.get() + itemStack.getCount());
            // TODO: 16/04/2023 add to chest data
        }
        if (WynnItemMatchers.isGearBox(itemStack)) {
            GearTier gearBoxTier = GearTier.fromComponent(itemStack.getHoverName());
            dryItemTiers.get().merge(gearBoxTier, 1, Integer::sum);
            dryItemTiers.touched();
            // TODO: 16/04/2023 store to chest
        }
        if (WynnItemMatchers.isGear(itemStack)) {
            dryItemTiers.get().merge(GearTier.NORMAL, 1, Integer::sum);
            dryItemTiers.touched();
            // TODO: 16/04/2023 store to chest
        }
        // TODO: 16/04/2023 add other data to chests
    }

    @SubscribeEvent
    public void onQuickMove(ChestMenuQuickMoveEvent event) {
        if (event.getContainerId() == nextExpectedLootContainerId) {
            nextExpectedLootContainerId = -2;
        }
    }
}
