/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.profile;

import com.wynntils.core.components.Models;
import com.wynntils.models.gear.type.IdentificationModifier;
import com.wynntils.utils.StringUtils;
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
        isInverted = Models.GearProfiles.isInverted(shortId);

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
