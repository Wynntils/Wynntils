/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi.profiles.item;

import com.google.gson.annotations.SerializedName;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;

public enum DamageType {
    @SerializedName("NEUTRAL")
    Neutral("❤", ChatFormatting.DARK_RED),
    @SerializedName("EARTH")
    Earth("✤", ChatFormatting.DARK_GREEN),
    @SerializedName("FIRE")
    Fire("✹", ChatFormatting.RED),
    @SerializedName("WATER")
    Water("❉", ChatFormatting.AQUA),
    @SerializedName("THUNDER")
    Thunder("✦", ChatFormatting.YELLOW),
    @SerializedName("AIR")
    Air("❋", ChatFormatting.WHITE);

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

    public static Pattern compileDamagePattern() {
        StringBuilder damageTypes = new StringBuilder();

        for (DamageType type : values()) {
            damageTypes.append(type.getSymbol());
        }

        return Pattern.compile("-(.*?) ([" + damageTypes + "])");
    }
}
