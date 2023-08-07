/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

import com.wynntils.core.WynntilsMod;
import com.wynntils.models.elements.type.Powder;
import com.wynntils.models.stats.StatCalculator;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.type.Pair;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;

// FIXME: GearInstance is missing Powder Specials...
public record GearInstance(
        List<StatActualValue> identifications,
        List<Powder> powders,
        int rerolls,
        Optional<Float> overallQuality,
        ShinyStatistic shinyStat) {
    public static GearInstance create(
            GearInfo gearInfo,
            List<StatActualValue> identifications,
            List<Powder> powders,
            int rerolls,
            Optional<Pair<String, Long>> shinyStat) {
        ShinyStatistic shinyStatistic = new ShinyStatistic(shinyStat);
        return new GearInstance(
                identifications, powders, rerolls, calculateOverallQuality(gearInfo, identifications), shinyStatistic);
    }

    private static Optional<Float> calculateOverallQuality(GearInfo gearInfo, List<StatActualValue> identifications) {
        DoubleSummaryStatistics percents = identifications.stream()
                .filter(actualValue -> {
                    // We do not include values that cannot possibly change
                    StatPossibleValues possibleValues = gearInfo.getPossibleValues(actualValue.statType());
                    if (possibleValues == null) {
                        WynntilsMod.warn("Error:" + gearInfo.name() + " claims to have identification "
                                + actualValue.statType());
                        return false;
                    }
                    return !possibleValues.range().isFixed()
                            && possibleValues.range().inRange(actualValue.value());
                })
                .mapToDouble(actualValue -> {
                    StatPossibleValues possibleValues = gearInfo.getPossibleValues(actualValue.statType());
                    return StatCalculator.getPercentage(actualValue, possibleValues);
                })
                .summaryStatistics();
        if (percents.getCount() == 0) return Optional.empty();

        return Optional.of((float) percents.getAverage());
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

    public void updateShinyStat(String name, long value, boolean force) {
        if (this.shinyStat.isStatPresent()) {
            String shinyStatName = this.shinyStat.getName();
            if (!shinyStatName.equals(name)) {
                WynntilsMod.warn("name mismatch between shiny statistics on previous data for item and updated item");
                return;
            }
            this.shinyStat.update(name, value);
        } else if (force) {
            this.shinyStat.update(name, value);
        } else {
            WynntilsMod.warn("can't modify shiny statistic on item because value doesn't exist");
        }
    }

    public boolean hashShinyStat() {
        return this.shinyStat.isStatPresent();
    }

    public StatActualValue getActualValue(StatType statType) {
        return identifications.stream()
                .filter(s -> s.statType().equals(statType))
                .findFirst()
                .orElse(null);
    }
}
