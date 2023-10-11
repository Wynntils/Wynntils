/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.properties.GearTierItemProperty;

public class InsulatorItem extends GameItem implements GearTierItemProperty {
    private final GearTier gearTier;

    public InsulatorItem(int emeraldPrice, GearTier gearTier) {
        super(emeraldPrice);
        this.gearTier = gearTier;
    }

    @Override
    public GearTier getGearTier() {
        return gearTier;
    }

    @Override
    public String toString() {
        return "InsulatorItem{" + "gearTier=" + gearTier + ", emeraldPrice=" + emeraldPrice + '}';
    }
}
