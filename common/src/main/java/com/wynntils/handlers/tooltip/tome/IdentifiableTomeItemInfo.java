/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.tome;

import com.wynntils.handlers.tooltip.type.IdentifiableItemInfo;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.rewards.type.TomeInfo;
import com.wynntils.models.rewards.type.TomeInstance;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.Pair;
import java.util.List;

public final class IdentifiableTomeItemInfo implements IdentifiableItemInfo {
    private final TomeInfo tomeInfo;
    private final TomeInstance tomeInstance;

    private IdentifiableTomeItemInfo(TomeInfo tomeInfo, TomeInstance tomeInstance) {
        this.tomeInfo = tomeInfo;
        this.tomeInstance = tomeInstance;
    }

    public static IdentifiableTomeItemInfo from(TomeInfo tomeInfo, TomeInstance tomeInstance) {
        return new IdentifiableTomeItemInfo(tomeInfo, tomeInstance);
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
}
