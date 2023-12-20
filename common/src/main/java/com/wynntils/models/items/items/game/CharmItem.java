/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.rewards.type.CharmInfo;
import com.wynntils.models.rewards.type.CharmInstance;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.List;
import java.util.Optional;

public class CharmItem extends GameItem implements GearTierItemProperty, IdentifiableItemProperty {
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
    public String getName() {
        return charmInfo.name();
    }

    @Override
    public ClassType getRequiredClass() {
        // Charms are not class-specific
        return ClassType.NONE;
    }

    @Override
    public List<StatType> getVariableStats() {
        return charmInfo.variableStats().stream().map(Pair::a).toList();
    }

    @Override
    public List<StatActualValue> getIdentifications() {
        return charmInstance.identifications();
    }

    @Override
    public List<StatPossibleValues> getPossibleValues() {
        return charmInfo.variableStats().stream().map(Pair::b).toList();
    }

    @Override
    public RangedValue getIdentificationLevelRange() {
        return charmInfo.requirements().workingLevelRange();
    }

    @Override
    public boolean hasOverallValue() {
        return charmInstance != null && charmInstance.hasOverallValue();
    }

    @Override
    public boolean isPerfect() {
        return charmInstance != null && charmInstance.isPerfect();
    }

    @Override
    public boolean isDefective() {
        return charmInstance != null && charmInstance.isDefective();
    }

    @Override
    public float getOverallPercentage() {
        return charmInstance != null ? charmInstance.getOverallPercentage() : 0.0f;
    }

    @Override
    public String toString() {
        return "CharmItem{" + "charmInfo="
                + charmInfo + ", charmInstance="
                + charmInstance + ", rerolls="
                + rerolls + '}';
    }
}
