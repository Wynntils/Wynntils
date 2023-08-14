/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.emeralds.type.EmeraldUnits;
import com.wynntils.models.items.properties.EmeraldValuedItemProperty;

public class EmeraldItem extends GameItem implements EmeraldValuedItemProperty {
    private final int amount;
    private final EmeraldUnits unit;

    public EmeraldItem(int amount, EmeraldUnits unit) {
        this.amount = amount;
        this.unit = unit;
    }

    public int getAmount() {
        return amount;
    }

    public EmeraldUnits getUnit() {
        return unit;
    }

    @Override
    public int getEmeraldValue() {
        return amount * unit.getMultiplier();
    }

    @Override
    public String toString() {
        return "EmeraldItem{" + "amount=" + amount + ", unit=" + unit + '}';
    }
}
