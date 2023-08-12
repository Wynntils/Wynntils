/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.territories.type;

import net.minecraft.ChatFormatting;

public enum GuildResource {
    EMERALD(ChatFormatting.GREEN, "Emeralds", ""),
    ORE(ChatFormatting.WHITE, "Ores", "Ⓑ"),
    WOOD(ChatFormatting.GOLD, "Wood", "Ⓒ"),
    FISH(ChatFormatting.AQUA, "Fishes", "Ⓚ"),
    CROPS(ChatFormatting.YELLOW, "Crops", "Ⓙ");

    private final ChatFormatting color;
    private final String name;
    private final String symbol;

    GuildResource(ChatFormatting color, String name, String symbol) {
        this.color = color;
        this.name = name;
        this.symbol = symbol;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public String getPrettySymbol() {
        return color + symbol + (!symbol.isEmpty() ? " " : "");
    }
}
