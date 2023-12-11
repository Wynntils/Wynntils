/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.rewards.type.TomeInfo;
import com.wynntils.models.rewards.type.TomeInstance;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.Optional;

public class TomeItem extends GameItem implements GearTierItemProperty, IdentifiableItemProperty {
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
    public String getName() {
        return tomeInfo.name();
    }

    @Override
    public ClassType getRequiredClass() {
        // Tomes are not class-specific
        return null;
    }

    @Override
    public List<StatType> getVariableStats() {
        return tomeInfo.variableStats().stream().map(Pair::a).toList();
    }

    @Override
    public List<StatActualValue> getIdentifications() {
        return tomeInstance.identifications();
    }

    @Override
    public List<StatPossibleValues> getPossibleValues() {
        return tomeInfo.variableStats().stream().map(Pair::b).toList();
    }

    @Override
    public boolean hasOverallValue() {
        return tomeInstance != null && tomeInstance.hasOverallValue();
    }

    @Override
    public boolean isPerfect() {
        return tomeInstance != null && tomeInstance.isPerfect();
    }

    @Override
    public boolean isDefective() {
        return tomeInstance != null && tomeInstance.isDefective();
    }

    @Override
    public float getOverallPercentage() {
        return tomeInstance != null ? tomeInstance.getOverallPercentage() : 0.0f;
    }

    @Override
    public String toString() {
        return "TomeItem{" + "tomeInfo=" + tomeInfo + ", tomeInstance=" + tomeInstance + ", rerolls=" + rerolls + '}';
    }
}
