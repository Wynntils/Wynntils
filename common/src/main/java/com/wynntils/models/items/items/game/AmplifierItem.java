/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.NamedItemProperty;
import com.wynntils.models.items.properties.NumberedTierItemProperty;
import com.wynntils.utils.MathUtils;

public class AmplifierItem extends GameItem
        implements NumberedTierItemProperty, GearTierItemProperty, NamedItemProperty {
    private final int tier;

    public AmplifierItem(int tier) {
        this.tier = tier;
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public GearTier getGearTier() {
        return GearTier.LEGENDARY;
    }

    @Override
    public String getName() {
        return "Corkian Amplifier " + MathUtils.toRoman(tier);
    }

    @Override
    public String toString() {
        return "AmplifierItem{" + "tier=" + tier + '}';
    }
}
