/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.inventory.resync;

import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.wynn.ContainerUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Suitable for most inventories, but especially useful for "click-to-insert" UIs like blacksmiths and item identifiers,
 * where other resync strategies may be too risky. In theory, this should be safe to use in any inventory so long as
 * Wynncraft doesn't drastically change the way they handle inventory clicks.
 */
public final class EmptySwapContentBookResyncStrategy implements InventoryResyncStrategy {
    public static final EmptySwapContentBookResyncStrategy INSTANCE = new EmptySwapContentBookResyncStrategy();

    private EmptySwapContentBookResyncStrategy() {}

    @Override
    public InventoryResynchronizer getResynchronizer(AbstractContainerMenu menu) {
        ItemStack bookStack = McUtils.player().getInventory().getItem(InventoryUtils.CONTENT_BOOK_SLOT_NUM);
        if (bookStack.isEmpty()) return null;

        for (int slotNum = 0; slotNum < menu.slots.size(); slotNum++) {
            Slot slot = menu.getSlot(slotNum);
            if (slot.getItem().isEmpty() && slot.mayPlace(bookStack)) {
                return new Resynchronizer(menu, slotNum);
            }
        }
        return null;
    }

    private static final class Resynchronizer extends InventoryResynchronizer {
        private final int slotNum;

        private Resynchronizer(AbstractContainerMenu menu, int slotNum) {
            super(menu);
            this.slotNum = slotNum;
        }

        @Override
        public boolean isValid() {
            ItemStack bookStack = McUtils.player().getInventory().getItem(InventoryUtils.CONTENT_BOOK_SLOT_NUM);
            if (bookStack.isEmpty()) return false;
            Slot slot = menu.getSlot(slotNum);
            return slot.getItem().isEmpty() && slot.mayPlace(bookStack);
        }

        @Override
        public void resync() {
            ContainerUtils.sendSlotInteraction(menu, slotNum, ClickType.SWAP, InventoryUtils.CONTENT_BOOK_SLOT_NUM);
        }
    }
}
