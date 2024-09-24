/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.inventory.resync;

import com.wynntils.utils.wynn.ContainerUtils;
import com.wynntils.utils.wynn.ItemUtils;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;

/**
 * Suitable for bank-like inventories that use placeholder slots to block quick-move operations. In theory, this should
 * be safe to use in any inventory, since it'll fail-safe for any unsupported ones.
 */
public final class ClickPlaceholderSlotResyncStrategy implements InventoryResyncStrategy {
    public static final ClickPlaceholderSlotResyncStrategy INSTANCE = new ClickPlaceholderSlotResyncStrategy();

    private ClickPlaceholderSlotResyncStrategy() {}

    @Override
    public InventoryResynchronizer getResynchronizer(AbstractContainerMenu menu) {
        if (menu.slots.size() <= 36) return null; // Sanity check

        for (int slotNum = menu.slots.size() - 37; slotNum >= 0; slotNum--) {
            if (ItemUtils.isNonSlotPlaceholder(menu.getSlot(slotNum).getItem())) {
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
            return ItemUtils.isNonSlotPlaceholder(menu.getSlot(slotNum).getItem());
        }

        @Override
        public void resync() {
            ContainerUtils.sendSlotInteraction(menu, slotNum, ClickType.PICKUP, 0);
        }
    }
}
