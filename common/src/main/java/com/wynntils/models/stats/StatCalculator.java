/*
 * Copyright © Wynntils 2023.
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

    public static RangedValue calculateInternalRollRange(StatPossibleValues possibleValues, int value, int stars) {
        // This code finds the lowest possible and highest possible rolls that result in the current
        // value (inclusive).

        int baseValue = possibleValues.baseValue();
        double lowerRawRollBound = (value * 100 - 50) / ((double) baseValue);
        // .5 is rounded up, so we need to add .49
        double higherRawRollBound = (value * 100 + 49) / ((double) baseValue);

        StatCalculationInfo statCalculationInfo =
                possibleValues.statType().getStatCalculationInfo(possibleValues.baseValue());

        if (baseValue < 0) {
            // Swap the bounds, since we are calculating using the negative range
            double temp = lowerRawRollBound;
            lowerRawRollBound = higherRawRollBound;
            higherRawRollBound = temp;
        }

        // We can further bound the possible rolls using the star count
        int starMin = statCalculationInfo.range().low();
        int starMax = statCalculationInfo.range().high();

        // If present, use the starInternalRollRanges to further bound the possible rolls
        // (negative stats do not have starInternalRollRanges, we do not need to check for them)
        // (stars is -1 if we don't want stars to be taken into account)
        if (stars != -1 && statCalculationInfo.starInternalRollRanges().size() > stars) {
            RangedValue rangedValue =
                    statCalculationInfo.starInternalRollRanges().get(stars);
            starMin = rangedValue.low();
            starMax = rangedValue.high();
        }

        int lowerRollBound = (int) Math.max(Math.ceil(lowerRawRollBound), starMin);
        int higherRollBound = (int) Math.max(lowerRollBound, Math.min(Math.floor(higherRawRollBound), starMax));

        // Check if the bounds are in the correct order
        assert lowerRollBound <= higherRollBound;
        // Check if the bounds are valid
        assert Math.round(baseValue * lowerRollBound / 100d) == Math.round(baseValue * higherRollBound / 100d);
        // Check if the lowest bound is the actually lowest possible roll
        assert lowerRollBound == starMin
                || Math.round(baseValue * lowerRollBound / 100d) != Math.round(baseValue * (lowerRollBound - 1) / 100d);
        // Check if the highest bound is the actually highest possible roll
        assert higherRollBound == starMax
                || Math.round(baseValue * higherRollBound / 100d)
                        != Math.round(baseValue * (higherRollBound + 1) / 100d);

        return RangedValue.of(lowerRollBound, higherRollBound);
    }

    public static int calculateStarsFromInternalRoll(StatType statType, int baseValue, int internalRoll) {
        // Star calculation reference, from salted:
        // https://forums.wynncraft.com/threads/about-the-little-asterisks.147931/#post-1654183
        StatCalculationInfo statCalculationInfo = statType.getStatCalculationInfo(baseValue);

        // If the stat is treated as inverted, we need to invert the base value
        // Note: This behavior could not be tested as of writing,
        //       since no stat is treated as inverted with a negative base value
        if (baseValue < 0 && statType.treatAsInverted()) {
            statCalculationInfo = statType.getStatCalculationInfo(-baseValue);
        }

        for (int stars = 0; stars < statCalculationInfo.starInternalRollRanges().size(); stars++) {
            RangedValue rangedValue =
                    statCalculationInfo.starInternalRollRanges().get(stars);
            if (rangedValue.inRange(internalRoll)) {
                return stars;
            }
        }

        return 0;
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
        boolean treatAsNegative = possibleValues.statType().treatAsInverted();

        int allCases =
                statCalculationInfo.range().high() - statCalculationInfo.range().low() + 1;

        // Internal roll range for maxiumum value
        // Do not confuse this with a "3 star" roll, aka perfect internal roll
        RangedValue perfectInternalRollRange = calculateInternalRollRange(
                possibleValues,
                treatAsNegative
                        ? possibleValues.range().low()
                        : possibleValues.range().high(),
                -1);
        int perfectCases = perfectInternalRollRange.high() - perfectInternalRollRange.low() + 1;

        return ((double) perfectCases) / allCases * 100;
    }

    public static double getDecreaseChance(StatActualValue actualValue, StatPossibleValues possibleValues) {
        assert !possibleValues.range().isFixed();

        StatCalculationInfo statCalculationInfo =
                possibleValues.statType().getStatCalculationInfo(possibleValues.baseValue());
        boolean treatAsNegative = possibleValues.statType().treatAsInverted();

        // This code finds the lowest possible and highest possible rolls that achieve the correct
        // result (inclusive). Then, it calculates the chance where we can get a lower roll
        RangedValue internalRollRange = actualValue.internalRoll();

        int allCases =
                statCalculationInfo.range().high() - statCalculationInfo.range().low() + 1;
        int decreaseCases = treatAsNegative
                ? statCalculationInfo.range().high() - internalRollRange.high()
                : internalRollRange.low() - statCalculationInfo.range().low();

        return ((double) decreaseCases) / allCases * 100;
    }

    public static double getIncreaseChance(StatActualValue actualValue, StatPossibleValues possibleValues) {
        assert !possibleValues.range().isFixed();

        StatCalculationInfo statCalculationInfo =
                possibleValues.statType().getStatCalculationInfo(possibleValues.baseValue());
        boolean treatAsNegative = possibleValues.statType().treatAsInverted();

        // This code finds the lowest possible and highest possible rolls that achieve the correct
        // result (inclusive). Then, it calculates the chance where we can get a higher roll
        RangedValue internalRollRange = actualValue.internalRoll();

        int allCases =
                statCalculationInfo.range().high() - statCalculationInfo.range().low() + 1;
        int increaseCases = treatAsNegative
                ? internalRollRange.low() - statCalculationInfo.range().low()
                : statCalculationInfo.range().high() - internalRollRange.high();

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
