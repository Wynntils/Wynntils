/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.handleditems.items.game;

import com.wynntils.utils.CappedValue;
import com.wynntils.wynn.handleditems.properties.GearTierItemProperty;
import com.wynntils.wynn.handleditems.properties.UsesItemPropery;
import com.wynntils.wynn.objects.profiles.item.GearTier;

public class TrinketItem extends GameItem implements GearTierItemProperty, UsesItemPropery {
    private final String trinketName;
    private final GearTier gearTier;
    private final CappedValue uses;

    public TrinketItem(String trinketName, GearTier gearTier, CappedValue uses) {
        this.trinketName = trinketName;
        this.gearTier = gearTier;
        this.uses = uses;
    }

    public TrinketItem(String trinketName, GearTier gearTier) {
        this.trinketName = trinketName;
        this.gearTier = gearTier;
        this.uses = null;
    }

    public String getTrinketName() {
        return trinketName;
    }

    public CappedValue getUses() {
        return uses;
    }

    @Override
    public GearTier getGearTier() {
        return gearTier;
    }

    @Override
    public String toString() {
        return "TrinketItem{" + "trinketName='"
                + trinketName + '\'' + ", gearTier="
                + gearTier + ", uses="
                + uses + '}';
    }
}
