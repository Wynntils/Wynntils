/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemsharing;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class SavedItemsMenu extends AbstractContainerMenu {
    private static final int MAX_ITEMS = 49;

    private SavedItemsMenu(Container container) {
        super(null, 101);

        int xPos = 24;
        int yPos = 25;
        int slotsOnCurrentRow = 0;

        for (int i = 0; i < MAX_ITEMS; i++) {
            this.addSlot(new Slot(container, i, xPos, yPos));

            xPos += 18;
            slotsOnCurrentRow++;

            if (slotsOnCurrentRow == 7) {
                xPos = 24;
                yPos += 18;
                slotsOnCurrentRow = 0;
            }
        }
    }

    public static SavedItemsMenu create() {
        return new SavedItemsMenu(new SimpleContainer(MAX_ITEMS));
    }

    public void clear() {
        for (int i = 0; i < MAX_ITEMS; i++) {
            this.setItem(i, 0, ItemStack.EMPTY);
        }
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
