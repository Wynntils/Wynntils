/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.items.properties.EmeraldValuedItemProperty;
import com.wynntils.models.items.properties.NumberedTierItemProperty;

public class EmeraldPouchItem extends GameItem implements NumberedTierItemProperty, EmeraldValuedItemProperty {
    private final int tier;
    private final int value;

    public EmeraldPouchItem(int tier, int value) {
        this.tier = tier;
        this.value = value;
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
