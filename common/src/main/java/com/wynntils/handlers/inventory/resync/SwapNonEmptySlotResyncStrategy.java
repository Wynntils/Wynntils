/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.inventory.resync;

import com.wynntils.utils.wynn.ContainerUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

/**
 * Suitable for inventories with no special interaction handling (e.g. no "click-to-insert" UIs, no filtering) such as
 * the player inventory and reward containers. Relies on the fact that SWAP interactions are not supported on Wynncraft,
 * but this may potentially be risky if that ever changes on Wynncraft's side. Should not be used for filtered
 * inventories because it may prompt an annoying error message in the chat. MUST NOT be used for click-to-insert UIs, as
 * it runs the risk of unintentionally inserting items!
 */
public final class SwapNonEmptySlotResyncStrategy implements InventoryResyncStrategy {
    public static final SwapNonEmptySlotResyncStrategy INSTANCE = new SwapNonEmptySlotResyncStrategy();

    private SwapNonEmptySlotResyncStrategy() {}

    @Override
    public InventoryResynchronizer getResynchronizer(AbstractContainerMenu menu) {
        for (int slotNum = 0; slotNum < menu.slots.size(); slotNum++) {
            Slot slot = menu.slots.get(slotNum);
            if (!slot.getItem().isEmpty() && !isInteractable(slot)) {
                return new Resynchronizer(menu, slotNum);
            }
        }
        return null;
    }

    private static boolean isInteractable(Slot slot) {
        if (!(slot.container instanceof Inventory)) return false;
        int slotNum = slot.getContainerSlot();
        return slotNum == InventoryUtils.COMPASS_SLOT_NUM || slotNum == InventoryUtils.CONTENT_BOOK_SLOT_NUM;
    }

    private static final class Resynchronizer extends InventoryResynchronizer {
        private final int slotNum;

        private Resynchronizer(AbstractContainerMenu menu, int slotNum) {
            super(menu);
            this.slotNum = slotNum;
        }

        @Override
        public boolean isValid() {
            return !menu.getSlot(slotNum).getItem().isEmpty();
        }

        @Override
        public void resync() {
            ContainerUtils.sendSlotInteraction(menu, slotNum, ClickType.SWAP, InventoryUtils.CONTENT_BOOK_SLOT_NUM);
        }
    }
}
