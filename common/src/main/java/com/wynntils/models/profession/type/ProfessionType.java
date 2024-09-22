/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.type;

import com.wynntils.core.persisted.config.NullableConfig;
import java.util.List;
import java.util.Locale;

public enum ProfessionType implements NullableConfig {
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

    public static List<ProfessionType> craftingProfessionTypes() {
        return List.of(ALCHEMISM, ARMOURING, COOKING, JEWELING, SCRIBING, TAILORING, WEAPONSMITHING, WOODWORKING);
    }
}
