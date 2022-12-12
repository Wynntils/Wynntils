/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects.profiles.item;

import com.wynntils.core.WynntilsMod;
import com.wynntils.utils.StringUtils;
import com.wynntils.wynn.item.IdentificationOrderer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This code is largely based off info provided in
 * https://forums.wynncraft.com/threads/how-identifications-are-calculated.128923/
 */
public class IdentificationProfile {
    private static final Map<String, IdentificationModifier> typeMap = new HashMap<>();

    private final IdentificationModifier type;
    private final int baseValue;
    private final boolean isFixed;
    private transient boolean isInverted;
    private transient int min;
    private transient int max;

    public IdentificationProfile(IdentificationModifier type, int baseValue, boolean isFixed) {
        this.type = type;
        this.baseValue = baseValue;
        this.isFixed = isFixed;
    }

    public void calculateMinMax(String shortId) {
        isInverted = IdentificationOrderer.INSTANCE.isInverted(shortId);

        if (isFixed || (-1 <= baseValue && baseValue <= 1)) {
            min = baseValue;
            max = baseValue;
            return;
        }

        boolean positive = (baseValue > 0) ^ isInverted;
        min = (int) Math.round(baseValue * (positive ? 0.3 : 1.3));
        max = (int) Math.round(baseValue * (positive ? 1.3 : 0.7));
    }

    public void registerIdType(String name) {
        if (typeMap.containsKey(name)) return;
        typeMap.put(name, type);
    }

    public IdentificationModifier getType() {
        return type;
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }

    public int getBaseValue() {
        return baseValue;
    }

    public boolean isInverted() {
        return isInverted;
    }

    public boolean isFixed() {
        return isFixed;
    }

    public boolean hasConstantValue() {
        return isFixed || min == max;
    }

    public static IdentificationModifier getTypeFromName(String name) {
        return typeMap.get(name);
    }

    public record ReidentificationChances(double decrease, double remain, double increase) {

        private ReidentificationChances flipIf(boolean flip) {
            if (flip) return new ReidentificationChances(increase, remain, decrease);

            return this;
        }
    }

    /**
     * Return the chances for this identification to decrease/remain the same/increase after
     * reidentification
     *
     * @param currentValue The current value of this identification
     * @param isInverted If true, `decrease` will be the chance to go up (become better) and vice
     *     versa with `increase`. Likewise, the stars are accounted as inverted
     * @return A {@link ReidentificationChances} of the result (All from 0 to 1)
     */
    public ReidentificationChances getChances(int currentValue, int starCount) {
        // Accounts for bounds - api isn't updated. Furthermore, there does exist the fact
        // that some items that have had its stats shifted from positive to negative to
        // break the bounds
        if (currentValue > max) {
            return new ReidentificationChances(1d, 0d, 0d).flipIf(isInverted);
        } else if (currentValue < min) {
            return new ReidentificationChances(0d, 0d, 1d).flipIf(isInverted);
        }

        if (hasConstantValue()) {
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

    /** @return The chance for this identification to become perfect (From 0 to 1) */
    public double getPerfectChance() {
        return 1 / (baseValue > 0 ? 101d : 61d);
    }

    /**
     * @param currentValue Current value of this identification
     * @return true if this is a valid value (If false, the API is probably wrong)
     */
    public boolean isInvalidValue(int currentValue) {
        return isInverted ? (currentValue > min || currentValue < max) : (currentValue > max || currentValue < min);
    }

    public static String getAsLongName(String shortName) {
        if (shortName.startsWith("raw")) {
            shortName = shortName.substring(3);
            shortName = Character.toLowerCase(shortName.charAt(0)) + shortName.substring(1);
        }

        StringBuilder nameBuilder = new StringBuilder();
        for (char c : shortName.toCharArray()) {
            if (Character.isUpperCase(c)) nameBuilder.append(" ").append(c);
            else nameBuilder.append(c);
        }

        return StringUtils.capitalizeFirst(nameBuilder.toString())
                .replaceAll("\\bXp\\b", "XP")
                .replaceAll("\\bX P\\b", "XP");
    }

    public static String getAsShortName(String longIdName, boolean raw) {
        String[] splitName = longIdName.split(" ");
        StringBuilder result = new StringBuilder(raw ? "raw" : "");
        for (String r : splitName) {
            result.append(Character.toUpperCase(r.charAt(0)))
                    .append(r.substring(1).toLowerCase(Locale.ROOT));
        }

        return StringUtils.uncapitalizeFirst(
                StringUtils.capitalizeFirst(result.toString()).replaceAll("\\bXP\\b", "Xp"));
    }
}
