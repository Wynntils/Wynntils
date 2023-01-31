/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gearinfo;

import com.wynntils.core.WynntilsMod;
import com.wynntils.models.stats.type.StatActualValue;
import com.wynntils.models.stats.type.StatPossibleValues;
import com.wynntils.utils.type.RangedValue;

public final class GearCalculator {
    public static float getPercent(StatActualValue actualValue, StatPossibleValues possibleValues) {
        int max = possibleValues.range().high() - possibleValues.range().low();
        int current = actualValue.value() - possibleValues.range().low();

        return (float) current / max * 100.0f;
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
        // FIXME
        return RangedValue.NONE;
    }

    // FIXME: This should be a method, not a class...
    public static class RecollCalculator {
        private final double decrease;
        private final double increase;
        private final double perfect;

        protected RecollCalculator(StatPossibleValues possibleValues, double decrease, double increase) {
            this.decrease = decrease;
            this.increase = increase;
            this.perfect = getPerfectChance(possibleValues);
        }

        public static RecollCalculator calculateChances(
                StatPossibleValues possibleValues, StatActualValue actualValue) {
            boolean isInverted = possibleValues.stat().showAsInverted();
            int baseValue = possibleValues.baseValue();
            // Accounts for bounds - api isn't updated. Furthermore, there does exist the fact
            // that some items that have had its stats shifted from positive to negative to
            // break the bounds
            // FIXME: This is probably completely broken. Rewrite!!!
            if (actualValue.value() > possibleValues.range().high()) {
                return new RecollCalculator(possibleValues, 1d, 0d).flipIf(isInverted, possibleValues);
            } else if (actualValue.value() < possibleValues.range().low()) {
                return new RecollCalculator(possibleValues, 0d, 1d).flipIf(isInverted, possibleValues);
            }

            if (possibleValues.range().isFixed()) {
                return new RecollCalculator(possibleValues, 0d, 0d).flipIf(isInverted, possibleValues);
            }

            // This code finds the lowest possible and highest possible rolls that achieve the correct
            // result (inclusive). Then, it finds the average decrease and increase afterwards

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
                        return new RecollCalculator(possibleValues, 100 / 101d, 0d);
                    }
                    default -> WynntilsMod.warn("Invalid star count of " + actualValue.stars());
                }

                double lowerRollBound = Math.max(Math.ceil(lowerRawRollBound), starMin);
                double higherRollBound = Math.min(Math.ceil(higherRawRollBound) - 1, starMax);

                double avg = (lowerRollBound + higherRollBound) / 2d;

                return new RecollCalculator(possibleValues, (avg - 30) / 101d, (130 - avg) / 101d)
                        .flipIf(isInverted, possibleValues);
            } else {
                double lowerRollBound = Math.min(Math.ceil(lowerRawRollBound) - 1, 130);
                double higherRollBound = Math.max(Math.ceil(higherRawRollBound), 70);

                double avg = (lowerRollBound + higherRollBound) / 2d;

                return new RecollCalculator(possibleValues, (130 - avg) / 61d, (avg - 70) / 61d)
                        .flipIf(isInverted, possibleValues);
            }
        }

        /** @return The chance for this identification to become perfect (From 0 to 1) */
        private static double getPerfectChance(StatPossibleValues possibleValues) {
            return 1 / (possibleValues.baseValue() > 0 ? 101d : 61d);
        }

        private RecollCalculator flipIf(boolean flip, StatPossibleValues possibleValues) {
            if (flip) return new RecollCalculator(possibleValues, increase, decrease);

            return this;
        }

        public double getDecrease() {
            return decrease;
        }

        public double getIncrease() {
            return increase;
        }

        public double getPerfect() {
            return perfect;
        }
    }
}
