/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats;

import com.wynntils.core.WynntilsMod;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatCalculationInfo;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;

public final class StatCalculator {
    // This constant is used to verify the calculated internal rolls
    // Enable this in development environments to check if the calculated internal rolls are correct
    private static final boolean VERIFY_CALCULATED_ROLLS = false;

    public static RangedValue calculatePossibleValuesRange(int baseValue, boolean preIdentified, StatType statType) {
        if (preIdentified) {
            // This is actually a single, fixed value
            return RangedValue.of(baseValue, baseValue);
        } else {
            int low;
            int high;

            StatCalculationInfo statCalculationInfo = statType.getStatCalculationInfo(baseValue);
            RoundingMode roundingMode = statCalculationInfo.roundingMode();

            // We can be really precise (and slow) here, since we are calculating this once, when initializing items
            low = new BigDecimal(baseValue)
                    .multiply(BigDecimal.valueOf(statCalculationInfo.range().low()))
                    .divide(BigDecimal.valueOf(100), roundingMode)
                    .setScale(0, roundingMode)
                    .intValue();
            high = new BigDecimal(baseValue)
                    .multiply(BigDecimal.valueOf(statCalculationInfo.range().high()))
                    .divide(BigDecimal.valueOf(100), roundingMode)
                    .setScale(0, roundingMode)
                    .intValue();

            // When calculating using the negative range, we need to swap the bounds
            if (high < low) {
                int temp = low;
                low = high;
                high = temp;
            }

            if (statCalculationInfo.minimumValue().isPresent()) {
                low = Math.max(low, statCalculationInfo.minimumValue().get());
            }
            if (statCalculationInfo.maximumValue().isPresent()) {
                high = Math.min(high, statCalculationInfo.maximumValue().get());
            }

            return RangedValue.of(low, high);
        }
    }

    public static int calculateStatValue(int internalRoll, StatPossibleValues possibleValues) {
        StatCalculationInfo statCalculationInfo =
                possibleValues.statType().getStatCalculationInfo(possibleValues.baseValue());
        RoundingMode roundingMode = statCalculationInfo.roundingMode();

        int value = new BigDecimal(possibleValues.baseValue())
                .multiply(BigDecimal.valueOf(internalRoll))
                .divide(BigDecimal.valueOf(100), roundingMode)
                .setScale(0, roundingMode)
                .intValue();

        if (value == 0) {
            // If we get to 0, use 1 or -1 instead
            value = (int) Math.signum(possibleValues.baseValue());
        }

        return value;
    }

    public static Pair<Integer, Integer> getDisplayRange(
            StatPossibleValues possibleValues, boolean showBestValueLastAlways) {
        StatType statType = possibleValues.statType();
        RangedValue valueRange = possibleValues.range();
        boolean isGood = valueRange.low() > 0;
        Pair<Integer, Integer> displayRange;
        int first;
        int last;
        if (showBestValueLastAlways || isGood) {
            first = valueRange.low();
            last = valueRange.high();
        } else {
            // Emulate Wynncraft behavior by showing the value closest to zero first
            first = valueRange.high();
            last = valueRange.low();
        }
        // We store "inverted" stats (spell costs) as positive numbers internally,
        // but need to display them as negative numbers
        if (statType.calculateAsInverted()) {
            first = -first;
            last = -last;
        }
        displayRange = Pair.of(first, last);
        return displayRange;
    }

    public static float getPercentage(StatActualValue actualValue, StatPossibleValues possibleValues) {
        int min = possibleValues.range().low();
        int max = possibleValues.range().high();

        if (actualValue.statType().treatAsInverted()) {
            // Inverted stats have the highest internal rolls when they have the worst effects
            // This is the opposite of normal stats, so we calculate the percentage by subtracting from the base range
            return 100 - MathUtils.inverseLerp(min, max, actualValue.value()) * 100;
        }

        return MathUtils.inverseLerp(min, max, actualValue.value()) * 100;
    }

    public static double getPerfectChance(StatPossibleValues possibleValues) {
        StatCalculationInfo statCalculationInfo =
                possibleValues.statType().getStatCalculationInfo(possibleValues.baseValue());

        int min = statCalculationInfo.range().low();
        int max = statCalculationInfo.range().high();

        int totalCases = max - min + 1;

        return 100.0 / totalCases;
    }

    public static double getDecreaseChance(StatActualValue actualValue, StatPossibleValues possibleValues) {
        assert !possibleValues.range().isFixed();

        StatCalculationInfo statCalculationInfo =
                possibleValues.statType().getStatCalculationInfo(possibleValues.baseValue());
        boolean treatAsNegative = possibleValues.statType().treatAsInverted() ^ possibleValues.baseValue() < 0;

        int allCases =
                statCalculationInfo.range().high() - statCalculationInfo.range().low() + 1;
        int decreaseCases = treatAsNegative
                ? statCalculationInfo.range().high()
                : statCalculationInfo.range().low();

        return ((double) decreaseCases) / allCases * 100;
    }

    public static double getIncreaseChance(StatActualValue actualValue, StatPossibleValues possibleValues) {
        assert !possibleValues.range().isFixed();

        StatCalculationInfo statCalculationInfo =
                possibleValues.statType().getStatCalculationInfo(possibleValues.baseValue());
        boolean treatAsNegative = possibleValues.statType().treatAsInverted() ^ possibleValues.baseValue() < 0;

        int allCases =
                statCalculationInfo.range().high() - statCalculationInfo.range().low() + 1;
        int increaseCases = treatAsNegative
                ? statCalculationInfo.range().low()
                : statCalculationInfo.range().high();

        return ((double) increaseCases) / allCases * 100;
    }

    public static Optional<Float> calculateOverallQuality(
            String itemName, List<StatPossibleValues> possibleValuesList, List<StatActualValue> identifications) {
        DoubleSummaryStatistics percents = identifications.stream()
                .filter(actualValue -> {
                    // We do not include values that cannot possibly change
                    StatPossibleValues possibleValues = possibleValuesList.stream()
                            .filter(possibleValue -> possibleValue.statType().equals(actualValue.statType()))
                            .findFirst()
                            .orElse(null);
                    if (possibleValues == null) {
                        WynntilsMod.warn(
                                "Error:" + itemName + " claims to have identification " + actualValue.statType());
                        return false;
                    }
                    return !possibleValues.range().isFixed()
                            && possibleValues.range().inRange(actualValue.value());
                })
                .mapToDouble(actualValue -> {
                    StatPossibleValues possibleValues = possibleValuesList.stream()
                            .filter(possibleValue -> possibleValue.statType().equals(actualValue.statType()))
                            .findFirst()
                            .orElse(null);
                    return StatCalculator.getPercentage(actualValue, possibleValues);
                })
                .summaryStatistics();
        if (percents.getCount() == 0) return Optional.empty();

        return Optional.of((float) percents.getAverage());
    }
}
