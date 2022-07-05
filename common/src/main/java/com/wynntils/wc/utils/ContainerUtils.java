/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public class ContainerUtils {

    public static boolean isLootChest(Screen screen) {
        return screen instanceof ContainerScreen containerScreen
                && containerScreen.getTitle().getString().contains("Loot Chest ");
    }

    public static boolean isLootOrRewardChest(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen)) return false;

        String title = screen.getTitle().getString();
        return title.startsWith("Loot Chest")
                || title.startsWith("Daily Rewards")
                || title.contains("Objective Rewards");
    }

    public static NonNullList<ItemStack> getItems(Screen screen) {
        if (screen instanceof AbstractContainerScreen<?> containerScreen) {
            return containerScreen.getMenu().getItems();
        }

        // Defensive programming, should not really happen
        return NonNullList.create();
    }
}
