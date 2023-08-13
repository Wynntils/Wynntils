/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.gearviewer;

import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class GearViewerMenu extends AbstractContainerMenu {
    private GearViewerMenu(Container container) {
        super(null, 100);

        this.addSlot(new Slot(container, 0, 78, 67));
        for (int i = 0; i < 4; i++) {
            this.addSlot(new Slot(container, i + 1, 11, 11 + i * 18));
        }
    }

    private void setHeldItem(ItemStack heldItem) {
        this.getSlot(0).set(heldItem);
    }

    private void setArmorItems(List<ItemStack> armorItems) {
        for (int i = 0; i < 4; i++) {
            if (i > armorItems.size()) break;

            this.getSlot(i + 1).set(armorItems.get(i));
        }
    }

    public static GearViewerMenu create(ItemStack heldItem, List<ItemStack> armorItems) {
        GearViewerMenu menu = new GearViewerMenu(new SimpleContainer(5));
        menu.setHeldItem(heldItem);
        menu.setArmorItems(armorItems);
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
