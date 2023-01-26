/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear;

import com.wynntils.core.WynntilsMod;
import com.wynntils.models.gear.profile.IdentificationProfile;

public record ReidentificationChances(double decrease, double remain, double increase) {
    public static ReidentificationChances getChances(IdentificationProfile idProfile, int currentValue, int starCount) {
        boolean isInverted = idProfile.isInverted();
        int baseValue = idProfile.getBaseValue();
        // Accounts for bounds - api isn't updated. Furthermore, there does exist the fact
        // that some items that have had its stats shifted from positive to negative to
        // break the bounds
        if (currentValue > idProfile.getMax()) {
            return new ReidentificationChances(1d, 0d, 0d).flipIf(isInverted);
        } else if (currentValue < idProfile.getMin()) {
            return new ReidentificationChances(0d, 0d, 1d).flipIf(isInverted);
        }

        if (idProfile.hasConstantValue()) {
            return new ReidentificationChances(0d, 1d, 0d).flipIf(isInverted);
        }

        // This code finds the lowest possible and highest possible rolls that achieve the correct
        // result (inclusive). Then, it finds the average decrease and increase afterwards

        // Note that due to rounding, a bound may not actually be a possible roll
        // if it results in a value that is exactly .5, which then rounds up/down

        double lowerRawRollBound = (currentValue * 100 - 50) / ((double) baseValue);
        double higherRawRollBound = (currentValue * 100 + 50) / ((double) baseValue);

        if (baseValue > 0) {
            // We can further bound the possible rolls using the star count
            int starMin = 30;
            int starMax = 130;

            switch (starCount) {
                case 0:
                    starMin = 30;
                    starMax = 100;
                    break;
                case 1:
                    starMin = 101;
                    starMax = 124;
                    break;
                case 2:
                    starMin = 125;
                    starMax = 129;
                    break;
                case 3:
                    return new ReidentificationChances(100 / 101d, 1 / 101d, 0d);
                default:
                    WynntilsMod.warn("Invalid star count of " + starCount);
            }

            double lowerRollBound = Math.max(Math.ceil(lowerRawRollBound), starMin);
            double higherRollBound = Math.min(Math.ceil(higherRawRollBound) - 1, starMax);

            double avg = (lowerRollBound + higherRollBound) / 2d;

            return new ReidentificationChances((avg - 30) / 101d, 1 / 101d, (130 - avg) / 101d).flipIf(isInverted);
        } else {
            double lowerRollBound = Math.min(Math.ceil(lowerRawRollBound) - 1, 130);
            double higherRollBound = Math.max(Math.ceil(higherRawRollBound), 70);

            double avg = (lowerRollBound + higherRollBound) / 2d;

            return new ReidentificationChances((130 - avg) / 61d, 1 / 61d, (avg - 70) / 61d).flipIf(isInverted);
        }
    }

    private ReidentificationChances flipIf(boolean flip) {
        if (flip) return new ReidentificationChances(increase, remain, decrease);

        return this;
    }
}
