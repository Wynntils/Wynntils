/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.type;

import java.util.Locale;

public enum ProfessionType {
    WOODCUTTING("Woodcutting", "Ⓒ"),
    MINING("Mining", "Ⓑ"),
    FISHING("Fishing", "Ⓚ"),
    FARMING("Farming", "Ⓙ"),

    // crafting
    ALCHEMISM("Alchemism", "Ⓛ"),
    ARMOURING("Armouring", "Ⓗ"),
    COOKING("Cooking", "Ⓐ"),
    JEWELING("Jeweling", "Ⓓ"),
    SCRIBING("Scribing", "Ⓔ"),
    TAILORING("Tailoring", "Ⓕ"),
    WEAPONSMITHING("Weaponsmithing", "Ⓖ"),
    WOODWORKING("Woodworking", "Ⓘ");

    private final String professionName;
    private final String professionIconChar;

    ProfessionType(String professionName, String professionIconChar) {
        this.professionName = professionName;
        this.professionIconChar = professionIconChar;
    }

    public String getDisplayName() {
        return professionName;
    }

    public String getProfessionIconChar() {
        return professionIconChar;
    }

    public static ProfessionType fromString(String type) {
        try {
            return valueOf(type.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
