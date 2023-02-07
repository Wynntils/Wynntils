/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.UsesItemPropery;
import com.wynntils.utils.type.CappedValue;

public class CraftedConsumableItem extends GameItem implements UsesItemPropery, GearTierItemProperty {
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

    public GearTier getGearTier() {
        return GearTier.CRAFTED;
    }

    @Override
    public String toString() {
        return "CraftedConsumableItem{" + "name='" + name + '\'' + ", uses=" + uses + '}';
    }
}
