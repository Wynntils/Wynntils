/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.trademarket;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * This is the same as {@link net.minecraft.world.inventory.ChestMenu}
 * but we do not create the final row of slots in addChestGrid so that
 * we maintain the look of a 9x6 container, but without populating
 * the final row with slots.
 */
public class TradeMarketMenu extends AbstractContainerMenu {
    private final Container container = new SimpleContainer(45);
    private final int containerRows = 6;

    private TradeMarketMenu(int containerId, Inventory playerInventory) {
        super(MenuType.GENERIC_9x6, containerId);
        container.startOpen(playerInventory.player);
        this.addChestGrid(container, 8, 18);
        int j = 18 + this.containerRows * 18 + 13;
        this.addStandardInventorySlots(playerInventory, 8, j);
    }

    private void addChestGrid(Container container, int x, int y) {
        for (int i = 0; i < this.containerRows - 1; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(container, j + i * 9, x + j * 18, y + i * 18));
            }
        }
    }

    public static TradeMarketMenu create(int containerId, Inventory playerInventory) {
        return new TradeMarketMenu(containerId, playerInventory);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (index < this.containerRows * 9) {
                if (!this.moveItemStackTo(itemStack2, this.containerRows * 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemStack2, 0, this.containerRows * 9, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public int getRowCount() {
        return this.containerRows;
    }
}
