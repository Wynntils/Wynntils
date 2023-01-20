/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.items.properties.UsesItemPropery;
import com.wynntils.utils.CappedValue;

public class ManaPotionItem extends GameItem implements UsesItemPropery {
    private final CappedValue uses;

    public ManaPotionItem(CappedValue uses) {
        this.uses = uses;
    }

    @Override
    public CappedValue getUses() {
        return uses;
    }

    @Override
    public String toString() {
        return "ManaPotionItem{" + "uses=" + uses + '}';
    }
}
