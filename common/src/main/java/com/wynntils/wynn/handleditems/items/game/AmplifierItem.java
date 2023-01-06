/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.items.game;

import com.wynntils.wynn.handleditems.properties.GearTierItemProperty;
import com.wynntils.wynn.handleditems.properties.NumberedTierItemProperty;
import com.wynntils.wynn.objects.profiles.item.ItemTier;

public class AmplifierItem extends GameItem implements NumberedTierItemProperty, GearTierItemProperty {
    private final int tier;

    public AmplifierItem(int tier) {
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }

    public ItemTier getGearTier() {
        return ItemTier.LEGENDARY;
    }

    @Override
    public String toString() {
        return "AmplifierItem{" + "tier=" + tier + '}';
    }
}
