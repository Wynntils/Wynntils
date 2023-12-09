/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.charm;

import com.wynntils.handlers.tooltip.type.IdentifiableItemInfo;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.rewards.type.CharmInfo;
import com.wynntils.models.rewards.type.CharmInstance;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.util.List;

public final class IdentifiableCharmItemInfo implements IdentifiableItemInfo {
    private final CharmInfo charmInfo;
    private final CharmInstance charmInstance;

    private IdentifiableCharmItemInfo(CharmInfo charmInfo, CharmInstance charmInstance) {
        this.charmInfo = charmInfo;
        this.charmInstance = charmInstance;
    }

    public static IdentifiableCharmItemInfo from(CharmInfo charmInfo, CharmInstance charmInstance) {
        return new IdentifiableCharmItemInfo(charmInfo, charmInstance);
    }

    @Override
    public String getName() {
        return charmInfo.name();
    }

    @Override
    public ClassType getRequiredClass() {
        // Charms are not class-specific
        return null;
    }

    @Override
    public List<StatType> getVariableStats() {
        return charmInfo.identificationStream().map(Pair::a).toList();
    }

    @Override
    public List<StatActualValue> getIdentifications() {
        return charmInstance.identifications();
    }

    @Override
    public List<StatPossibleValues> getPossibleValues() {
        return charmInfo.identificationStream().map(Pair::b).toList();
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
}
