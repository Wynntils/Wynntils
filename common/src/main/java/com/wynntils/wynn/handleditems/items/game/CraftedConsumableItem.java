/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.items.game;

import com.wynntils.utils.CappedValue;
import com.wynntils.wynn.handleditems.properties.GearTierItemProperty;
import com.wynntils.wynn.handleditems.properties.UsesItemPropery;
import com.wynntils.wynn.objects.profiles.item.ItemTier;

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

    public ItemTier getGearTier() {
        return ItemTier.CRAFTED;
    }

    @Override
    public String toString() {
        return "CraftedConsumableItem{" + "name='" + name + '\'' + ", uses=" + uses + '}';
    }
}
