/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects;

import java.util.Locale;

public enum Skill {
    STRENGTH("✤"),
    DEXTERITY("✦"),
    INTELLIGENCE("✽"),
    DEFENSE("✹"),
    AGILITY("❋");

    private final String symbol;

    Skill(String symbol) {
        this.symbol = symbol;
    }

    public static Skill fromString(String str) {
        try {
            return Skill.valueOf(str.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getSymbol() {
        return symbol;
    }
}
