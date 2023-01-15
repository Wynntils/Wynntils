/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects;

import com.wynntils.utils.StringUtils;
import java.util.Locale;
import net.minecraft.ChatFormatting;

public enum Skill {
    STRENGTH("✤", ChatFormatting.DARK_GREEN),
    DEXTERITY("✦", ChatFormatting.YELLOW),
    INTELLIGENCE("✽", ChatFormatting.AQUA),
    DEFENCE("✹", ChatFormatting.RED, "defense"), // Note! Must be spelled with "C" to match in-game
    AGILITY("❋", ChatFormatting.WHITE);

    private final String symbol;
    private final ChatFormatting color;
    private final String apiName;
    private final String displayName;

    Skill(String symbol, ChatFormatting color, String apiName) {
        this.symbol = symbol;
        this.color = color;
        this.apiName = apiName;
        this.displayName = StringUtils.capitalized(this.name());
    }

    Skill(String symbol, ChatFormatting color) {
        this.symbol = symbol;
        this.color = color;
        this.apiName = this.name().toLowerCase(Locale.ROOT);
        this.displayName = StringUtils.capitalized(this.name());
    }

    public static Skill fromString(String str) {
        try {
            return Skill.valueOf(str.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static boolean isSkill(String idName) {
        for (Skill skill : values()) {
            if (idName.equals(skill.getDisplayName())) {
                return true;
            }
        }
        return false;
    }

    public String getSymbol() {
        return symbol;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getApiName() {
        return apiName;
    }
}
