/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.statemanaged;

import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigManager;
import com.wynntils.core.features.StateManagedFeature;
import com.wynntils.core.webapi.profiles.item.ItemTier;
import com.wynntils.mc.event.ChestMenuQuickMoveEvent;
import com.wynntils.mc.event.ContainerSetSlotEvent;
import com.wynntils.mc.event.MenuEvent;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.wynn.item.parsers.WynnItemMatchers;
import com.wynntils.wynn.utils.ContainerUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// FIXME: This feature is only needed because we do not have a way to save function data persistently. Remove this when
//        we add persistent data storage other than configs.
public class PersistentFunctionsFeature extends StateManagedFeature {
    private static final int LOOT_CHEST_ITEM_COUNT = 27;

    public static PersistentFunctionsFeature INSTANCE;

    @Config(visible = false)
    public int dryCount = 0;

    @Config(visible = false)
    public int dryBoxes = 0;

    private int nextExpectedLootContainerId = -2;

    @SubscribeEvent
    public void onMenuOpened(MenuEvent.MenuOpenedEvent event) {
        if (ContainerUtils.isLootOrRewardChest(ComponentUtils.getUnformatted(event.getTitle()))) {
            nextExpectedLootContainerId = event.getContainerId();
            System.out.println("nextExpectedLootContainerId = " + nextExpectedLootContainerId);

            dryCount++;
        }
    }

    @SubscribeEvent
    public void onSetSlot(ContainerSetSlotEvent event) {
        if (event.getContainerId() != nextExpectedLootContainerId) return;
        if (event.getSlot() >= LOOT_CHEST_ITEM_COUNT) return;

        ItemStack itemStack = event.getItemStack();

        System.out.println("itemStack.getHoverName() = " + itemStack.getHoverName());

        if (!WynnItemMatchers.isUnidentified(itemStack)) return;

        ItemTier itemTier = ItemTier.fromComponent(itemStack.getHoverName());

        if (itemTier == ItemTier.MYTHIC) {
            dryBoxes = 0;
            dryCount = 0;
        } else {
            dryBoxes += 1;
        }

        ConfigManager.saveConfig();
    }

    @SubscribeEvent
    public void onQuickMove(ChestMenuQuickMoveEvent event) {
        if (event.getContainerId() == nextExpectedLootContainerId) {
            nextExpectedLootContainerId = -2;
        }
    }
}
