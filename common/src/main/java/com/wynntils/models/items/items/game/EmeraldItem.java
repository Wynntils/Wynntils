/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.emeralds.type.EmeraldUnits;
import com.wynntils.models.items.properties.EmeraldValuedItemProperty;
import java.util.function.Supplier;

public class EmeraldItem extends GameItem implements EmeraldValuedItemProperty {
    private final Supplier<Integer> amountSupplier;
    private final EmeraldUnits unit;

    public EmeraldItem(int emeraldPrice, Supplier<Integer> amountSupplier, EmeraldUnits unit) {
        super(emeraldPrice);
        this.amountSupplier = amountSupplier;
        this.unit = unit;
    }

    public int getAmount() {
        return amountSupplier.get();
    }

    public EmeraldUnits getUnit() {
        return unit;
    }

    @Override
    public int getEmeraldValue() {
        return getAmount() * unit.getMultiplier();
    }

    @Override
    public String toString() {
        return "EmeraldItem{" + "amount=" + getAmount() + ", unit=" + unit + ", emeraldPrice=" + emeraldPrice + '}';
    }
}
