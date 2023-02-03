/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.utils.type.RangedValue;

public class GearBoxItem extends GameItem implements GearTierItemProperty {
    private final GearType gearType;
    private final GearTier gearTier;
    private final RangedValue levelRange;

    public GearBoxItem(GearType gearType, GearTier gearTier, RangedValue levelRange) {
        this.gearType = gearType;
        this.gearTier = gearTier;
        this.levelRange = levelRange;
    }

    public GearType getGearType() {
        return gearType;
    }

    @Override
    public GearTier getGearTier() {
        return gearTier;
    }

    public RangedValue getLevelRange() {
        return levelRange;
    }

    @Override
    public String toString() {
        return "GearBoxItem{" + "gearType="
                + gearType + ", gearTier="
                + gearTier + ", levelRange='"
                + levelRange + '\'' + '}';
    }
}
