/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.raid.raids.RaidKind;

public class CrafterBagItem extends GameItem implements GearTierItemProperty {
    private final GearTier gearTier;
    private final RaidKind raidKind;

    public CrafterBagItem(GearTier gearTier, RaidKind raidKind) {
        this.gearTier = gearTier;
        this.raidKind = raidKind;
    }

    @Override
    public GearTier getGearTier() {
        return gearTier;
    }

    public RaidKind getRaidKind() {
        return raidKind;
    }

    @Override
    public String toString() {
        return "CrafterBagItem{" + "gearTier=" + gearTier + ", raidKind=" + raidKind + '}';
    }
}
