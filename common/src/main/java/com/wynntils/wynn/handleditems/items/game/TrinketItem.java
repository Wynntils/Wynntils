/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.items.game;

import com.wynntils.utils.CappedValue;
import com.wynntils.wynn.handleditems.properties.GearTierItemProperty;
import com.wynntils.wynn.handleditems.properties.UsesItemPropery;
import com.wynntils.wynn.objects.profiles.item.ItemTier;

public class TrinketItem extends GameItem implements GearTierItemProperty, UsesItemPropery {
    private final String trinketName;
    private final ItemTier itemTier;
    private final CappedValue uses;

    public TrinketItem(String trinketName, ItemTier itemTier, CappedValue uses) {
        this.trinketName = trinketName;
        this.itemTier = itemTier;
        this.uses = uses;
    }

    public TrinketItem(String trinketName, ItemTier itemTier) {
        this.trinketName = trinketName;
        this.itemTier = itemTier;
        this.uses = null;
    }

    public String getTrinketName() {
        return trinketName;
    }

    public ItemTier getItemTier() {
        return itemTier;
    }

    public CappedValue getUses() {
        return uses;
    }

    @Override
    public ItemTier getGearTier() {
        return itemTier;
    }

    @Override
    public String toString() {
        return "TrinketItem{" + "trinketName='"
                + trinketName + '\'' + ", itemTier="
                + itemTier + ", uses="
                + uses + '}';
    }
}
