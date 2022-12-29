/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Model;
import com.wynntils.features.statemanaged.DataStorageFeature;
import com.wynntils.mc.event.ChestMenuQuickMoveEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.objects.profiles.item.ItemTier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class LootChestModel extends Model {
    private static final int LOOT_CHEST_ITEM_COUNT = 27;

    private int nextExpectedLootContainerId = -2;

    @SubscribeEvent
    public void onMenuOpened(MenuEvent.MenuOpenedEvent event) {
        if (Managers.Container.isLootChest(ComponentUtils.getUnformatted(event.getTitle()))) {
            nextExpectedLootContainerId = event.getContainerId();

            DataStorageFeature.INSTANCE.dryCount++;
            Managers.Config.saveConfig();
        }
    }

    @SubscribeEvent
    public void onSetSlot(ContainerSetSlotEvent event) {
        if (event.getContainerId() != nextExpectedLootContainerId) return;
        if (event.getSlot() >= LOOT_CHEST_ITEM_COUNT) return;

        ItemStack itemStack = event.getItemStack();

        if (!WynnItemMatchers.isGearBox(itemStack)) return;

        ItemTier itemTier = ItemTier.fromComponent(itemStack.getHoverName());

        if (itemTier == ItemTier.MYTHIC) {
            DataStorageFeature.INSTANCE.dryBoxes = 0;
            DataStorageFeature.INSTANCE.dryCount = 0;
        } else {
            DataStorageFeature.INSTANCE.dryBoxes += 1;
        }

        Managers.Config.saveConfig();
    }

    @SubscribeEvent
    public void onQuickMove(ChestMenuQuickMoveEvent event) {
        if (event.getContainerId() == nextExpectedLootContainerId) {
            nextExpectedLootContainerId = -2;
        }
    }
}
