/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.type;

import net.minecraft.world.item.ItemStack;

public class SetInstance {
    private final int wynncraftCount;
    private int trueCount = 0;
    private final ItemStack relevantItem;

    public SetInstance(int wynncraftCount, ItemStack relevantItem) {
        this.wynncraftCount = wynncraftCount;
        this.relevantItem = relevantItem;
    }

    public int getWynncraftCount() {
        return wynncraftCount;
    }

    public int getTrueCount() {
        return trueCount;
    }

    public ItemStack getRelevantItem() {
        return relevantItem;
    }

    public void incrementTrueCount() {
        this.trueCount++;
    }
}
