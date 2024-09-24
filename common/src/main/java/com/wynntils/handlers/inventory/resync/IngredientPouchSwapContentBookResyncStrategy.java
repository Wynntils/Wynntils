/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.inventory.resync;

import com.wynntils.utils.wynn.ContainerUtils;
import com.wynntils.utils.wynn.InventoryUtils;
import com.wynntils.utils.wynn.ItemUtils;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * Suitable for the player inventory. Should not be used in filtered inventories because it prompts an annoying error
 * message in the chat. MUST NOT be used for reward containers, as it runs the risk of opening the ingredient pouch!
 */
public final class IngredientPouchSwapContentBookResyncStrategy implements InventoryResyncStrategy {
    public static final IngredientPouchSwapContentBookResyncStrategy INSTANCE =
            new IngredientPouchSwapContentBookResyncStrategy();

    private IngredientPouchSwapContentBookResyncStrategy() {}

    @Override
    public InventoryResynchronizer getResynchronizer(AbstractContainerMenu menu) {
        int slotNum =
                menu instanceof InventoryMenu ? InventoryUtils.INGREDIENT_POUCH_SLOT_NUM : (menu.slots.size() - 32);
        return ItemUtils.isIngredientPouch(menu.getSlot(slotNum).getItem()) ? new Resynchronizer(menu, slotNum) : null;
    }

    private static final class Resynchronizer extends InventoryResynchronizer {
        private final int slotNum;

        private Resynchronizer(AbstractContainerMenu menu, int slotNum) {
            super(menu);
            this.slotNum = slotNum;
        }

        @Override
        public boolean isValid() {
            return ItemUtils.isIngredientPouch(menu.getSlot(slotNum).getItem());
        }

        @Override
        public void resync() {
            ContainerUtils.sendSlotInteraction(menu, slotNum, ClickType.SWAP, InventoryUtils.CONTENT_BOOK_SLOT_NUM);
        }
    }
}
