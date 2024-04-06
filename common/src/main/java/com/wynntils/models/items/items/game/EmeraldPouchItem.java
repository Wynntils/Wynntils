/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.emeralds.EmeraldModel;
import com.wynntils.models.items.properties.EmeraldValuedItemProperty;
import com.wynntils.models.items.properties.NamedItemProperty;
import com.wynntils.models.items.properties.NumberedTierItemProperty;
import com.wynntils.utils.MathUtils;

public class EmeraldPouchItem extends GameItem
        implements NamedItemProperty, NumberedTierItemProperty, EmeraldValuedItemProperty {
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
            capacity = (tier - 6) * EmeraldModel.LIQUID_EMERALD_STACK_VALUE;
        } else if (tier >= 4) {
            capacity = maxValue * EmeraldModel.LIQUID_EMERALD_VALUE;
        } else {
            capacity = maxValue * EmeraldModel.EMERALD_BLOCK_VALUE;
        }
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
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
    public String getName() {
        return "Emerald Pouch [Tier " + MathUtils.toRoman(tier) + "]";
    }

    @Override
    public String toString() {
        return "EmeraldPouchItem{" + "tier=" + tier + ", value=" + value + '}';
    }
}
