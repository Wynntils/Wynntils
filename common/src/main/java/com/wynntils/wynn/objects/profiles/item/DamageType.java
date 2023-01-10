/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects.profiles.item;

import com.wynntils.utils.StringUtils;
import java.util.Locale;
import net.minecraft.ChatFormatting;

public enum DamageType {
    NEUTRAL("✣", ChatFormatting.GOLD),
    EARTH("✤", ChatFormatting.DARK_GREEN),
    FIRE("✹", ChatFormatting.RED),
    WATER("❉", ChatFormatting.AQUA),
    THUNDER("✦", ChatFormatting.YELLOW),
    AIR("❋", ChatFormatting.WHITE);

    private final String symbol;
    private final ChatFormatting color;

    DamageType(String symbol, ChatFormatting color) {
        this.symbol = symbol;
        this.color = color;
    }

    public String getSymbol() {
        return symbol;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public static DamageType fromSymbol(String symbol) {
        for (DamageType type : values()) {
            if (type.symbol.equals(symbol)) return type;
        }
        return null;
    }

    @Override
    public String toString() {
        return StringUtils.capitalizeFirst(name().toLowerCase(Locale.ROOT));
    }
}
