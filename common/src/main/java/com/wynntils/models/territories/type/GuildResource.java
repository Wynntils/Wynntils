/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.territories.type;

import net.minecraft.ChatFormatting;

public enum GuildResource {
    EMERALDS(ChatFormatting.GREEN, "Emeralds", ""),
    ORE(ChatFormatting.WHITE, "Ore", "Ⓑ"),
    WOOD(ChatFormatting.GOLD, "Wood", "Ⓒ"),
    FISH(ChatFormatting.AQUA, "Fish", "Ⓚ"),
    CROPS(ChatFormatting.YELLOW, "Crops", "Ⓙ");

    private final ChatFormatting color;
    private final String name;
    private final String symbol;

    GuildResource(ChatFormatting color, String name, String symbol) {
        this.color = color;
        this.name = name;
        this.symbol = symbol;
    }

    public static GuildResource fromName(String name) {
        for (GuildResource resource : values()) {
            if (resource.getName().equalsIgnoreCase(name)) {
                return resource;
            }
        }
        return null;
    }

    public static GuildResource fromSymbol(String symbol) {
        for (GuildResource resource : values()) {
            if (resource.getSymbol().equalsIgnoreCase(symbol)) {
                return resource;
            }
        }
        return null;
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
        return color + symbol + (symbol.isEmpty() ? "" : " ");
    }

    public boolean isMaterialResource() {
        return this != EMERALDS;
    }
}
