/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.rewards.type.CharmInfo;
import com.wynntils.models.rewards.type.CharmInstance;
import java.util.Optional;

public class CharmItem extends GameItem implements GearTierItemProperty {
    private final CharmInfo charmInfo;
    private final CharmInstance charmInstance;
    private final int rerolls;

    public CharmItem(CharmInfo charmInfo, CharmInstance charmInstance, int rerolls) {
        this.charmInfo = charmInfo;
        this.charmInstance = charmInstance;
        this.rerolls = rerolls;
    }

    public CharmInfo getCharmInfo() {
        return charmInfo;
    }

    public Optional<CharmInstance> getCharmInstance() {
        return Optional.ofNullable(charmInstance);
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
        return "CharmItem{" + "charmInfo="
                + charmInfo + ", charmInstance="
                + charmInstance + ", rerolls="
                + rerolls + '}';
    }
}
