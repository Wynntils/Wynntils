/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.game;

import com.wynntils.utils.CappedValue;

public class ConsumableItem extends GameItem {
    private final CappedValue charges;

    public ConsumableItem(CappedValue charges) {
        this.charges = charges;
    }

    public CappedValue getCharges() {
        return charges;
    }
}
