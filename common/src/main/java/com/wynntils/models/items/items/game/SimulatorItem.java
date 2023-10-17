/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.properties.GearTierItemProperty;

public class SimulatorItem extends GameItem implements GearTierItemProperty {
    private final GearTier gearTier;

    public SimulatorItem(GearTier gearTier) {
        this.gearTier = gearTier;
    }

    @Override
    public GearTier getGearTier() {
        return gearTier;
    }

    @Override
    public String toString() {
        return "SimulatorItem{" + "gearTier=" + gearTier + '}';
    }
}
