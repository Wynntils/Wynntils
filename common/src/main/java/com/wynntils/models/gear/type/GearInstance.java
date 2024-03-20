/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.ShinyStat;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatType;
import java.util.List;
import java.util.Optional;

// FIXME: GearInstance is missing Powder Specials...
public record GearInstance(
        List<StatActualValue> identifications,
        List<Powder> powders,
        int rerolls,
        Optional<Float> overallQuality,
        Optional<ShinyStat> shinyStat) {
    public static GearInstance create(
            GearInfo gearInfo,
            List<StatActualValue> identifications,
            List<Powder> powders,
            int rerolls,
            Optional<ShinyStat> shinyStat) {
        return new GearInstance(
                identifications,
                powders,
                rerolls,
                StatCalculator.calculateOverallQuality(
                        gearInfo.name(), gearInfo.getPossibleValueList(), identifications),
                shinyStat);
    }

    public boolean hasOverallValue() {
        return overallQuality.isPresent();
    }

    public float getOverallPercentage() {
        return overallQuality.orElse(0.0f);
    }

    public boolean isPerfect() {
        return overallQuality.orElse(0.0f) >= 100.0f;
    }

    public boolean isDefective() {
        return overallQuality.orElse(0.0f) <= 0.0f;
    }

    public StatActualValue getActualValue(StatType statType) {
        return identifications.stream()
                .filter(s -> s.statType().equals(statType))
                .findFirst()
                .orElse(null);
    }
}
