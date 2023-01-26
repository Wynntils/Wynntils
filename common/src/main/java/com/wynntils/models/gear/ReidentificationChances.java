/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear;

import com.wynntils.core.WynntilsMod;
import com.wynntils.models.gear.profile.IdentificationProfile;

public class ReidentificationChances {
    private final double decrease;
    private final double remain;
    private final double increase;
    private final double perfect;

    public ReidentificationChances(IdentificationProfile idProfile, double decrease, double remain, double increase) {
        this.decrease = decrease;
        this.remain = remain;
        this.increase = increase;
        this.perfect = getPerfectChance(idProfile);
    }

    public static ReidentificationChances calculateChances(IdentificationProfile idProfile, int currentValue, int starCount) {
        boolean isInverted = idProfile.isInverted();
        int baseValue = idProfile.getBaseValue();
        // Accounts for bounds - api isn't updated. Furthermore, there does exist the fact
        // that some items that have had its stats shifted from positive to negative to
        // break the bounds
        if (currentValue > idProfile.getMax()) {
            return new ReidentificationChances(idProfile, 1d, 0d, 0d).flipIf(isInverted, idProfile);
        } else if (currentValue < idProfile.getMin()) {
            return new ReidentificationChances(idProfile, 0d, 0d, 1d).flipIf(isInverted, idProfile);
        }

        if (idProfile.hasConstantValue()) {
            return new ReidentificationChances(idProfile, 0d, 1d, 0d).flipIf(isInverted, idProfile);
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
                    return new ReidentificationChances(idProfile, 100 / 101d, 1 / 101d, 0d);
                default:
                    WynntilsMod.warn("Invalid star count of " + starCount);
            }

            double lowerRollBound = Math.max(Math.ceil(lowerRawRollBound), starMin);
            double higherRollBound = Math.min(Math.ceil(higherRawRollBound) - 1, starMax);

            double avg = (lowerRollBound + higherRollBound) / 2d;

            return new ReidentificationChances(idProfile, (avg - 30) / 101d, 1 / 101d, (130 - avg) / 101d)
                    .flipIf(isInverted, idProfile);
        } else {
            double lowerRollBound = Math.min(Math.ceil(lowerRawRollBound) - 1, 130);
            double higherRollBound = Math.max(Math.ceil(higherRawRollBound), 70);

            double avg = (lowerRollBound + higherRollBound) / 2d;

            return new ReidentificationChances(idProfile, (130 - avg) / 61d, 1 / 61d, (avg - 70) / 61d)
                    .flipIf(isInverted, idProfile);
        }
    }

    /** @return The chance for this identification to become perfect (From 0 to 1) */
    private static double getPerfectChance(IdentificationProfile idProfile) {
        return 1 / (idProfile.getBaseValue() > 0 ? 101d : 61d);
    }

    private ReidentificationChances flipIf(boolean flip, IdentificationProfile idProfile) {
        if (flip) return new ReidentificationChances(idProfile, increase, remain, decrease);

        return this;
    }

    public double getDecrease() {
        return decrease;
    }

    public double getRemain() {
        return remain;
    }

    public double getIncrease() {
        return increase;
    }

    public double getPerfect() {
        return perfect;
    }
}
