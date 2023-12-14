/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.game;

import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.gear.type.GearInfo;
import com.wynntils.models.gear.type.GearInstance;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.gear.type.GearType;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.models.items.properties.GearTypeItemProperty;
import com.wynntils.models.items.properties.IdentifiableItemProperty;
import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.Optional;

public class GearItem extends GameItem
        implements GearTierItemProperty, GearTypeItemProperty, LeveledItemProperty, IdentifiableItemProperty {
    private final GearInfo gearInfo;
    private final GearInstance gearInstance;

    public GearItem(GearInfo gearInfo, GearInstance gearInstance) {
        this.gearInfo = gearInfo;
        this.gearInstance = gearInstance;
    }

    public GearInfo getGearInfo() {
        return gearInfo;
    }

    public Optional<GearInstance> getGearInstance() {
        return Optional.ofNullable(gearInstance);
    }

    public boolean isUnidentified() {
        return gearInstance == null;
    }

    @Override
    public GearTier getGearTier() {
        return gearInfo.tier();
    }

    @Override
    public GearType getGearType() {
        return gearInfo.type();
    }

    @Override
    public int getLevel() {
        return gearInfo.requirements().level();
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

    @Override
    public String toString() {
        return "GearItem{" + "gearInfo=" + gearInfo + ", gearInstance=" + gearInstance + '}';
    }
}
