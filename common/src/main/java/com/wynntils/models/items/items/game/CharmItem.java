/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.rewards.type.DeprecatedCharmInfo;
import com.wynntils.models.stats.type.StatActualValue;
import java.util.List;

public class CharmItem extends GameItem implements GearTierItemProperty {
    private final DeprecatedCharmInfo charmInfo;
    private final List<StatActualValue> identifications;
    private final int rerolls;

    public CharmItem(DeprecatedCharmInfo charmInfo, List<StatActualValue> identifications, int rerolls) {
        this.charmInfo = charmInfo;
        this.identifications = identifications;
        this.rerolls = rerolls;
    }

    public DeprecatedCharmInfo getCharmProfile() {
        return charmInfo;
    }

    public List<StatActualValue> getIdentifications() {
        return identifications;
    }

    public int getRerolls() {
        return rerolls;
    }

    @Override
    public GearTier getGearTier() {
        return charmInfo.tier();
    }

    @Override
    public String toString() {
        return "CharmItem{" + "charmProfile="
                + charmInfo + ", identifications="
                + identifications + ", rerolls="
                + rerolls + '}';
    }
}
