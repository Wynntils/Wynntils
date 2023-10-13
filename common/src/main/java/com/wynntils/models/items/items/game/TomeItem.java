/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.rewards.type.TomeInfo;
import com.wynntils.models.stats.type.StatActualValue;
import java.util.List;

public class TomeItem extends GameItem implements GearTierItemProperty {
    private final TomeInfo tomeInfo;
    private final List<StatActualValue> identifications;
    private final int rerolls;

    public TomeItem(int emeraldPrice, TomeInfo tomeInfo, List<StatActualValue> identifications, int rerolls) {
        super(emeraldPrice);
        this.tomeInfo = tomeInfo;
        this.identifications = identifications;
        this.rerolls = rerolls;
    }

    public TomeInfo getTomeProfile() {
        return tomeInfo;
    }

    public List<StatActualValue> getIdentifications() {
        return identifications;
    }

    public int getRerolls() {
        return rerolls;
    }

    @Override
    public GearTier getGearTier() {
        return tomeInfo.gearTier();
    }

    @Override
    public String toString() {
        return "TomeItem{" + "tomeInfo="
                + tomeInfo + ", identifications="
                + identifications + ", rerolls="
                + rerolls + ", emeraldPrice="
                + emeraldPrice + '}';
    }
}
