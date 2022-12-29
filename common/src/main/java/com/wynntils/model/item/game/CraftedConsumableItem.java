/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.model.item.game;

import com.wynntils.model.item.properties.UsesItemPropery;
import com.wynntils.utils.CappedValue;

public class CraftedConsumableItem extends GameItem implements UsesItemPropery {
    private final String name;
    private final CappedValue uses;

    public CraftedConsumableItem(String name, CappedValue uses) {
        this.name = name;
        this.uses = uses;
    }

    public String getName() {
        return name;
    }

    public CappedValue getUses() {
        return uses;
    }
}
