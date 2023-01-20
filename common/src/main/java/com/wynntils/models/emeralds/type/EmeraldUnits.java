/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.emeralds.type;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum EmeraldUnits {
    EMERALD(Items.EMERALD, EmeraldSymbols.EMERALDS, 1),
    EMERALD_BLOCK(Items.EMERALD_BLOCK, EmeraldSymbols.EB, 64),
    LE(Items.EXPERIENCE_BOTTLE, EmeraldSymbols.LE, 64 * 64);

    private final Item itemType;
    private final ItemStack itemStack;
    private final String symbol;
    private final int multiplier;

    EmeraldUnits(Item itemType, String symbol, int multiplier) {
        this.itemType = itemType;
        this.itemStack = new ItemStack(itemType);
        this.symbol = symbol;
        this.multiplier = multiplier;
    }

    public Item getItemType() {
        return itemType;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public static EmeraldUnits fromItemType(Item itemType) {
        for (EmeraldUnits unit : values()) {
            if (unit.getItemType() == itemType) {
                return unit;
            }
        }
        return null;
    }
}
