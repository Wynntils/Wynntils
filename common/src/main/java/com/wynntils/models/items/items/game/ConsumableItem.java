/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.items.properties.UsesItemPropery;
import com.wynntils.utils.type.CappedValue;

public class ConsumableItem extends GameItem implements UsesItemPropery {
    private final CappedValue uses;

    public ConsumableItem(CappedValue uses) {
        this.uses = uses;
    }

    public CappedValue getUses() {
        return uses;
    }

    @Override
    public String toString() {
        return "ConsumableItem{" + "uses=" + uses + '}';
    }
}
