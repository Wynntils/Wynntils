/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.tooltip.gear;

import com.wynntils.handlers.tooltip.type.IdentifiableItemInfo;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.Pair;
import java.util.List;

public final class IdentifiableGearItemInfo implements IdentifiableItemInfo {
    private final GearInfo gearInfo;
    private final GearInstance gearInstance;

    private IdentifiableGearItemInfo(GearInfo gearInfo, GearInstance gearInstance) {
        this.gearInfo = gearInfo;
        this.gearInstance = gearInstance;
    }

    public static IdentifiableGearItemInfo from(GearInfo gearInfo, GearInstance gearInstance) {
        return new IdentifiableGearItemInfo(gearInfo, gearInstance);
    }

    @Override
    public String getName() {
        return gearInfo.name();
    }

    @Override
    public ClassType getRequiredClass() {
        return gearInfo.requirements().classType().orElse(null);
    }

    @Override
    public List<StatType> getVariableStats() {
        return gearInfo.variableStats().stream().map(Pair::a).toList();
    }

    @Override
    public List<StatActualValue> getIdentifications() {
        if (gearInstance == null) return List.of();

        return gearInstance.identifications();
    }

    @Override
    public List<StatPossibleValues> getPossibleValues() {
        return gearInfo.variableStats().stream().map(Pair::b).toList();
    }

    @Override
    public boolean hasOverallValue() {
        return gearInstance != null && gearInstance.hasOverallValue();
    }

    @Override
    public boolean isPerfect() {
        return gearInstance != null && gearInstance.isPerfect();
    }

    @Override
    public boolean isDefective() {
        return gearInstance != null && gearInstance.isDefective();
    }

    @Override
    public float getOverallPercentage() {
        return gearInstance != null ? gearInstance.getOverallPercentage() : 0.0f;
    }
}
