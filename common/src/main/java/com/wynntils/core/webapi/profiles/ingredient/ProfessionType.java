/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi.profiles.ingredient;

import java.util.Locale;

public enum ProfessionType {
    WEAPONSMITHING("Weaponsmithing", "Ⓖ"),
    WOODWORKING("Woodworking", "Ⓘ"),
    ARMOURING("Armouring", "Ⓗ"),
    TAILORING("Tailoring", "Ⓕ"),
    JEWELING("Jeweling", "Ⓓ"),
    COOKING("Cooking", "Ⓐ"),
    ALCHEMISM("Alchemism", "Ⓛ"),
    SCRIBING("Scribing", "Ⓔ");

    final String professionName;
    final String professionIconChar;

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
        return switch (type.toLowerCase(Locale.ROOT)) {
            case "weaponsmithing" -> WEAPONSMITHING;
            case "woodworking" -> WOODWORKING;
            case "armouring" -> ARMOURING;
            case "tailoring" -> TAILORING;
            case "jeweling" -> JEWELING;
            case "cooking" -> COOKING;
            case "alchemism" -> ALCHEMISM;
            case "scribing" -> SCRIBING;
            default -> null;
        };
    }
}
