/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.items.game;

import com.wynntils.utils.CappedValue;
import com.wynntils.wynn.handleditems.properties.UsesItemPropery;

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
