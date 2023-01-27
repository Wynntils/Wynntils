/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.gear.type.CharmProfile;
import com.wynntils.models.gearinfo.type.GearTier;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.stats.type.StatActualValue;
import java.util.List;

public class CharmItem extends GameItem implements GearTierItemProperty {
    private final CharmProfile charmProfile;
    private final List<StatActualValue> identifications;
    private final int rerolls;

    public CharmItem(CharmProfile charmProfile, List<StatActualValue> identifications, int rerolls) {
        this.charmProfile = charmProfile;
        this.identifications = identifications;
        this.rerolls = rerolls;
    }

    public CharmProfile getCharmProfile() {
        return charmProfile;
    }

    public List<StatActualValue> getIdentifications() {
        return identifications;
    }

    public int getRerolls() {
        return rerolls;
    }

    @Override
    public GearTier getGearTier() {
        return charmProfile.tier();
    }

    @Override
    public String toString() {
        return "CharmItem{" + "charmProfile="
                + charmProfile + ", identifications="
                + identifications + ", rerolls="
                + rerolls + '}';
    }
}
