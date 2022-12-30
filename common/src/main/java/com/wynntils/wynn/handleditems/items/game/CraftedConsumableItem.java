/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.items.game;

import com.wynntils.utils.CappedValue;
import com.wynntils.wynn.handleditems.properties.UsesItemPropery;

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

    @Override
    public String toString() {
        return "CraftedConsumableItem{" + "name='" + name + '\'' + ", uses=" + uses + '}';
    }
}
