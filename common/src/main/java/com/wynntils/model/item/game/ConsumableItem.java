/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.game;

import com.wynntils.model.item.properties.UsesItemPropery;
import com.wynntils.utils.CappedValue;

public class ConsumableItem extends GameItem implements UsesItemPropery {
    private final CappedValue uses;

    public ConsumableItem(CappedValue uses) {
        this.uses = uses;
    }

    public CappedValue getUses() {
        return uses;
    }
}
