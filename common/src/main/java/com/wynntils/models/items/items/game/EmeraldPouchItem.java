/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.items.properties.EmeraldValuedItemProperty;
import com.wynntils.models.items.properties.NumberedTierItemProperty;

public class EmeraldPouchItem extends GameItem implements NumberedTierItemProperty, EmeraldValuedItemProperty {
    private static final int EMERALD_BLOCK = 64;
    private static final int LIQUID_EMERALD = 4096;
    private static final int LIQUID_EMERALD_STACK = 262144;

    private final int tier;
    private final int value;

    private final int capacity;

    public EmeraldPouchItem(int tier, int value) {
        this.tier = tier;
        this.value = value;

        int maxValue = 0;

        switch (tier % 3) {
            case 0 -> maxValue = 54;
            case 1 -> maxValue = 9;
            case 2 -> maxValue = 27;
        }

        if (tier >= 7) {
            capacity = (tier - 6) * LIQUID_EMERALD_STACK;
        } else if (tier >= 4) {
            capacity = maxValue * LIQUID_EMERALD;
        } else {
            capacity = maxValue * EMERALD_BLOCK;
        }
    }

    public int getCapacity() {
        return capacity;
    }

    public int getTier() {
        return tier;
    }

    public int getValue() {
        return value;
    }

    @Override
    public int getEmeraldValue() {
        return value;
    }

    @Override
    public String toString() {
        return "EmeraldPouchItem{" + "tier=" + tier + ", value=" + value + '}';
    }
}
