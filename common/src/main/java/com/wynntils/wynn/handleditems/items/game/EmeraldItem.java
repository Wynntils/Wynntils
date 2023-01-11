/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.items.game;

import com.wynntils.wynn.handleditems.properties.EmeraldValuedItemProperty;
import com.wynntils.wynn.objects.EmeraldUnits;

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
