/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats;

import com.wynntils.core.WynntilsMod;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.models.stats.type.StatType;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;

public final class StatCalculator {
    public static float getPercentage(StatActualValue actualValue, StatPossibleValues possibleValues) {
        int min = possibleValues.range().low();
        int max = possibleValues.range().high();

        float percentage = MathUtils.inverseLerp(min, max, actualValue.value()) * 100;
        return percentage;
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
        if (statType.showAsInverted()) {
            first = -first;
            last = -last;
        }
        displayRange = Pair.of(first, last);
        return displayRange;
    }

    public static RangedValue calculateRange(int baseValue, boolean preIdentified) {
        if (preIdentified) {
            // This is actually a single, fixed value
            return RangedValue.of(baseValue, baseValue);
        } else {
            if (baseValue > 0) {
                // Between 30% and 130% of base value, always at least 1
                int low = Math.max((int) Math.round(baseValue * 0.3), 1);
                int high = (int) Math.round(baseValue * 1.3);
                return RangedValue.of(low, high);
            } else {
                // Between 70% and 130% of base value, always at most -1
                // Round ties towards positive infinity (confirmed on Wynncraft)
                int low = (int) Math.round(baseValue * 1.3);
                int high = Math.min((int) Math.round(baseValue * 0.7), -1);
                return RangedValue.of(low, high);
            }
        }
    }

    public static int getStarsFromInternalRoll(int internalRoll) {
        // Star calculation reference, from salted:
        // https://forums.wynncraft.com/threads/about-the-little-asterisks.147931/#post-1654183
        int stars;
        if (internalRoll < 101) {
            stars = 0;
        } else if (internalRoll < 125) {
            stars = 1;
        } else if (internalRoll < 130) {
            stars = 2;
        } else {
            stars = 3;
        }
        return stars;
    }

    // Calculate the range of possible values for the internal roll for this stat
    public static RangedValue calculateInternalRoll(StatPossibleValues possibleValues, StatActualValue actualValue) {
        return getInnerRollRange(actualValue, possibleValues.baseValue());
    }

    public static double getPerfectChance(StatPossibleValues possibleValues) {
        // FIXME: This is the chance of getting a *** (3 star) roll, not the chance of
        // getting the maximum possible value. But for now, keep old behavior.
        double perfectChance = 1 / (possibleValues.baseValue() > 0 ? 101d : 61d) * 100;
        return perfectChance;
    }

    public static double getDecreaseChance(StatActualValue actualValue, StatPossibleValues possibleValues) {
        // If we are in any of these situations, our stat has "invalid" values. Maybe it is old?
        // If so, rerolling will have a certain outcome of 100% or 0% chance of decrease
        if (actualValue.value() > possibleValues.range().high()) {
            return 1d;
        } else if (actualValue.value() < possibleValues.range().low()) {
            return 0d;
        }

        // We should not be checking this if it is fixed, but handle that case as well
        if (possibleValues.range().isFixed()) {
            return 0d;
        }

        // This code finds the lowest possible and highest possible rolls that achieve the correct
        // result (inclusive). Then, it finds the average decrease and increase afterwards
        int baseValue = possibleValues.baseValue();
        RangedValue innerRollRange = getInnerRollRange(actualValue, baseValue);

        return getDecreaseFromInnerRoll(innerRollRange) * 100;
    }

    public static double getIncreaseChance(StatActualValue actualValue, StatPossibleValues possibleValues) {
        // If we are in any of these situations, our stat has "invalid" values. Maybe it is old?
        // If so, rerolling will have a certain outcome of 100% or 0% chance of decrease
        if (actualValue.value() > possibleValues.range().high()) {
            return 0d;
        } else if (actualValue.value() < possibleValues.range().low()) {
            return 1d;
        }

        // We should not be checking this if it is fixed, but handle that case as well
        if (possibleValues.range().isFixed()) {
            return 0d;
        }

        // This code finds the lowest possible and highest possible rolls that achieve the correct
        // result (inclusive). Then, it finds the average decrease and increase afterwards
        int baseValue = possibleValues.baseValue();
        RangedValue innerRollRange = getInnerRollRange(actualValue, baseValue);

        return getIncreaseFromInnerRoll(innerRollRange) * 100;
    }

    private static double getDecreaseFromInnerRoll(RangedValue innerRollRange) {
        double avg = (innerRollRange.low() + innerRollRange.high()) / 2d;
        if (innerRollRange.low() > 0) {
            return (avg - 30) / 101d;
        } else {
            return (130 - avg) / 61d;
        }
    }

    private static double getIncreaseFromInnerRoll(RangedValue innerRollRange) {
        double avg = (innerRollRange.low() + innerRollRange.high()) / 2d;

        if (innerRollRange.low() > 0) {
            return (130 - avg) / 101d;
        } else {
            return (avg - 70) / 61d;
        }
    }

    private static RangedValue getInnerRollRange(StatActualValue actualValue, int baseValue) {
        // Note that due to rounding, a bound may not actually be a possible roll
        // if it results in a value that is exactly .5, which then rounds up/down
        double lowerRawRollBound = (actualValue.value() * 100 - 50) / ((double) baseValue);
        double higherRawRollBound = (actualValue.value() * 100 + 50) / ((double) baseValue);

        if (baseValue > 0) {
            // We can further bound the possible rolls using the star count
            int starMin = 30;
            int starMax = 130;

            switch (actualValue.stars()) {
                case 0 -> {
                    starMin = 30;
                    starMax = 100;
                }
                case 1 -> {
                    starMin = 101;
                    starMax = 124;
                }
                case 2 -> {
                    starMin = 125;
                    starMax = 129;
                }
                case 3 -> {
                    return RangedValue.of(130, 130);
                }
                default -> WynntilsMod.warn("Invalid star count of " + actualValue.stars());
            }

            int lowerRollBound = (int) Math.max(Math.ceil(lowerRawRollBound), starMin);
            int higherRollBound = (int) Math.min(Math.ceil(higherRawRollBound) - 1, starMax);
            return RangedValue.of(lowerRollBound, higherRollBound);
        } else {
            int lowerRollBound = (int) Math.min(Math.ceil(lowerRawRollBound) - 1, 130);
            int higherRollBound = (int) Math.max(Math.ceil(higherRawRollBound), 70);
            return RangedValue.of(lowerRollBound, higherRollBound);
        }
    }
}
