/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum EmeraldUnits {
    EMERALD(new ItemStack(Items.EMERALD), EmeraldSymbols.EMERALDS),
    EMERALD_BLOCK(new ItemStack(Items.EMERALD_BLOCK), EmeraldSymbols.EB),
    LE(new ItemStack(Items.EXPERIENCE_BOTTLE), EmeraldSymbols.LE);

    private final ItemStack itemStack;
    private final String symbol;

    EmeraldUnits(ItemStack itemStack, String symbol) {
        this.itemStack = itemStack;
        this.symbol = symbol;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public String getSymbol() {
        return symbol;
    }
}
