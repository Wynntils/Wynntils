/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects;

import java.util.Locale;
import net.minecraft.ChatFormatting;

public enum Skill {
    STRENGTH("✤", ChatFormatting.DARK_GREEN),
    DEXTERITY("✦", ChatFormatting.YELLOW),
    INTELLIGENCE("✽", ChatFormatting.AQUA),
    DEFENCE("✹", ChatFormatting.RED),  // Note! Must be spelled with "C" to match in-game
    AGILITY("❋", ChatFormatting.WHITE);

    private final String symbol;
    private final ChatFormatting color;

    Skill(String symbol, ChatFormatting color) {
        this.symbol = symbol;
        this.color = color;
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

    public ChatFormatting getColor() {
        return color;
    }
}
