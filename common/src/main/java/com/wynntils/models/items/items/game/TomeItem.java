/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.rewards.type.TomeInfo;
import com.wynntils.models.rewards.type.TomeInstance;
import java.util.Optional;

public class TomeItem extends GameItem implements GearTierItemProperty {
    private final TomeInfo tomeInfo;
    private final TomeInstance tomeInstance;
    private final int rerolls;

    public TomeItem(TomeInfo tomeInfo, TomeInstance tomeInstance, int rerolls) {
        this.tomeInfo = tomeInfo;
        this.tomeInstance = tomeInstance;
        this.rerolls = rerolls;
    }

    public TomeInfo getTomeInfo() {
        return tomeInfo;
    }

    public Optional<TomeInstance> getTomeInstance() {
        return Optional.ofNullable(tomeInstance);
    }

    public int getRerolls() {
        return rerolls;
    }

    @Override
    public GearTier getGearTier() {
        return tomeInfo.tier();
    }

    @Override
    public String toString() {
        return "TomeItem{" + "tomeInfo=" + tomeInfo + ", tomeInstance=" + tomeInstance + ", rerolls=" + rerolls + '}';
    }
}
