/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.item.properties;

import com.wynntils.mc.objects.CustomColor;
import com.wynntils.wynn.item.GearItemStack;
import com.wynntils.wynn.item.WynnItemStack;
import com.wynntils.wynn.item.properties.type.HighlightProperty;
import com.wynntils.wynn.objects.profiles.item.ItemTier;

public class ItemTierProperty extends ItemProperty implements HighlightProperty {
    private final ItemTier tier;

    public ItemTierProperty(WynnItemStack item) {
        super(item);

        // parse tier
        if (item instanceof GearItemStack gearItem) {
            tier = gearItem.getItemProfile().getTier();
            return;
        }

        tier = ItemTier.fromComponent(item.getHoverName());
    }

    public ItemTier getTier() {
        return tier;
    }

    @Override
    public boolean isHighlightEnabled() {
        return false;
    }

    @Override
    public CustomColor getHighlightColor() {
        return CustomColor.NONE;
    }
}
