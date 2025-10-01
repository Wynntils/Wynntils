/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.playerviewer;

import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class PlayerViewerMenu extends AbstractContainerMenu {
    private PlayerViewerMenu(Container container) {
        super(null, 100);

        this.addSlot(new Slot(container, 0, 100, 63));
        int x = 28;
        int y = 9;
        for (int i = 0; i < 8; i++) {
            this.addSlot(new Slot(container, i + 1, x, y));

            if (i == 3) {
                x = 10;
                y = 9;
            } else {
                y += 18;
            }
        }
    }

    private void setHeldItem(ItemStack heldItem) {
        this.getSlot(0).set(heldItem);
    }

    private void setArmorItems(List<ItemStack> armorItems) {
        for (int i = 0; i < 4; i++) {
            if (i >= armorItems.size()) break;

            this.getSlot(i + 1).set(armorItems.get(i));
        }
    }

    private void setAccessoryItems(List<ItemStack> accessoryItems) {
        for (int i = 0; i < 4; i++) {
            if (i >= accessoryItems.size()) break;

            this.getSlot(i + 5).set(accessoryItems.get(i));
        }
    }

    public static PlayerViewerMenu create(
            ItemStack heldItem, List<ItemStack> armorItems, List<ItemStack> accessoryItems) {
        PlayerViewerMenu menu = new PlayerViewerMenu(new SimpleContainer(9));
        menu.setHeldItem(heldItem);
        menu.setArmorItems(armorItems);
        menu.setAccessoryItems(accessoryItems);
        return menu;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
