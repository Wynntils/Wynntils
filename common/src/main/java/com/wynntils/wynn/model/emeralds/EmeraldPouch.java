/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.emeralds;

import net.minecraft.world.item.ItemStack;

public final class EmeraldPouch {
    private final int slotNumber;
    private final ItemStack stack;

    EmeraldPouch(int slotNumber, ItemStack stack) {
        this.slotNumber = slotNumber;
        this.stack = stack;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public ItemStack getStack() {
        return stack;
    }
}
