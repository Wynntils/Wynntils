/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.TomeProfile;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.wynn.gear.types.GearIdentification;
import java.util.List;

public class TomeItem extends GameItem implements GearTierItemProperty {
    private final TomeProfile tomeProfile;
    private final List<GearIdentification> identifications;
    private final int rerolls;

    public TomeItem(TomeProfile tomeProfile, List<GearIdentification> identifications, int rerolls) {
        this.tomeProfile = tomeProfile;
        this.identifications = identifications;
        this.rerolls = rerolls;
    }

    public TomeProfile getTomeProfile() {
        return tomeProfile;
    }

    public List<GearIdentification> getIdentifications() {
        return identifications;
    }

    public int getRerolls() {
        return rerolls;
    }

    @Override
    public GearTier getGearTier() {
        return tomeProfile.gearTier();
    }

    @Override
    public String toString() {
        return "TomeItem{" + "tomeProfile="
                + tomeProfile + ", identifications="
                + identifications + ", rerolls="
                + rerolls + '}';
    }
}
