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

        // Note that due to rounding, a bound may not actually be a possible roll
        // if it results in a value that is exactly .5, which then rounds up/down
        int baseValue = possibleValues.baseValue();
        double lowerRawRollBound = (value * 100 - 50) / ((double) baseValue);
        double higherRawRollBound = (value * 100 + 50) / ((double) baseValue);

        StatCalculationInfo statCalculationInfo =
                possibleValues.statType().getStatCalculationInfo(possibleValues.baseValue());

        if (baseValue > 0) {
            // We can further bound the possible rolls using the star count
            int starMin = statCalculationInfo.range().low();
            int starMax = statCalculationInfo.range().high();

            // If present, use the starInternalRollRanges to further bound the possible rolls
            if (statCalculationInfo.starInternalRollRanges().size() < stars) {
                RangedValue rangedValue =
                        statCalculationInfo.starInternalRollRanges().get(stars);
                starMin = rangedValue.low();
                starMax = rangedValue.high();
            }

            int lowerRollBound = (int) Math.max(Math.ceil(lowerRawRollBound), starMin);
            int higherRollBound = (int) Math.min(Math.ceil(higherRawRollBound) - 1, starMax);
            return RangedValue.of(lowerRollBound, higherRollBound);
        } else {
            int lowerRollBound = (int) Math.min(
                    Math.ceil(lowerRawRollBound) - 1,
                    statCalculationInfo.range().high());
            int higherRollBound = (int) Math.max(
                    Math.ceil(higherRawRollBound), statCalculationInfo.range().low());
            return RangedValue.of(lowerRollBound, higherRollBound);
        }
    }

    public static int calculateStarsFromInternalRoll(StatType statType, int baseValue, int internalRoll) {
        // Star calculation reference, from salted:
        // https://forums.wynncraft.com/threads/about-the-little-asterisks.147931/#post-1654183
        StatCalculationInfo statCalculationInfo = statType.getStatCalculationInfo(baseValue);
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

        return MathUtils.inverseLerp(min, max, actualValue.value()) * 100;
    }

    public static double getPerfectChance(StatPossibleValues possibleValues) {
        // FIXME: This is the chance of getting a *** (3 star) roll, not the chance of
        // getting the maximum possible value. But for now, keep old behavior.
        return 1 / (possibleValues.baseValue() > 0 ? 101d : 61d) * 100;
    }

    public static double getDecreaseChance(StatActualValue actualValue, StatPossibleValues possibleValues) {
        assert !possibleValues.range().isFixed();

        // This code finds the lowest possible and highest possible rolls that achieve the correct
        // result (inclusive). Then, it finds the average decrease and increase afterwards
        RangedValue innerRollRange = actualValue.internalRoll();

        // FIXME: What we probably really want is the percentage of possible internal rolls
        // that is lower than innerRollRange.low, but do not change this for now.
        double result;
        double avg = (innerRollRange.low() + innerRollRange.high()) / 2d;
        if (innerRollRange.low() > 0) {
            result = (avg - 30) / 101d;
        } else {
            result = (130 - avg) / 61d;
        }
        return result * 100;
    }

    public static double getIncreaseChance(StatActualValue actualValue, StatPossibleValues possibleValues) {
        assert !possibleValues.range().isFixed();

        // This code finds the lowest possible and highest possible rolls that achieve the correct
        // result (inclusive). Then, it finds the average decrease and increase afterwards
        RangedValue innerRollRange = actualValue.internalRoll();

        // FIXME: What we probably really want is the percentage of possible internal rolls
        // that is higher than innerRollRange.high, but do not change this for now.
        double result;
        double avg = (innerRollRange.low() + innerRollRange.high()) / 2d;

        if (innerRollRange.low() > 0) {
            result = (130 - avg) / 101d;
        } else {
            result = (avg - 70) / 61d;
        }
        return result * 100;
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
