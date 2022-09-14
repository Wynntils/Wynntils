/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects;

import com.wynntils.mc.objects.CustomColor;
import com.wynntils.utils.StringUtils;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;

public enum Powder {
    EARTH('✤', 10, 2, ChatFormatting.DARK_GREEN, ChatFormatting.GREEN), // light and dark colors are swapped
    THUNDER('✦', 11, 14, ChatFormatting.YELLOW, ChatFormatting.GOLD),
    WATER('❉', 12, 6, ChatFormatting.AQUA, ChatFormatting.DARK_AQUA),
    FIRE('✹', 9, 1, ChatFormatting.RED, ChatFormatting.DARK_RED),
    AIR('❋', 8, 7, ChatFormatting.WHITE, ChatFormatting.GRAY);

    private final char symbol;
    private final int lowTierDamage;
    private final int highTierDamage;
    private final ChatFormatting lightColor;
    private final ChatFormatting darkColor;

    Powder(char symbol, int lowTierDamage, int highTierDamage, ChatFormatting lightColor, ChatFormatting darkColor) {
        this.symbol = symbol;
        this.lowTierDamage = lowTierDamage;
        this.highTierDamage = highTierDamage;
        this.lightColor = lightColor;
        this.darkColor = darkColor;
    }

    public char getSymbol() {
        return symbol;
    }

    public String getColorString() {
        return lightColor.toString();
    }

    public CustomColor getColor() {
        return CustomColor.fromInt(this.lightColor.getColor()).withAlpha(255);
    }

    public ChatFormatting getRawColor() {
        return lightColor;
    }

    public String getColoredSymbol() {
        return lightColor.toString() + symbol;
    }

    public String getLetterRepresentation() {
        return this.name().substring(0, 1).toLowerCase();
    }

    public static List<Powder> findPowders(String input) {
        List<Powder> foundPowders = new LinkedList<>();
        input.chars().forEach(ch -> {
            for (Powder powder : values()) {
                if (ch == powder.getSymbol()) {
                    foundPowders.add(powder);
                }
            }
        });

        return foundPowders;
    }

    public int getLowTierDamage() {
        return lowTierDamage;
    }

    public int getHighTierDamage() {
        return highTierDamage;
    }

    public ChatFormatting getLightColor() {
        return lightColor;
    }

    public ChatFormatting getDarkColor() {
        return darkColor;
    }

    public String getName() {
        return StringUtils.capitalizeFirst(this.name().toLowerCase(Locale.ROOT));
    }

    public Powder getOpposingElement() {
        return switch (this) {
            case EARTH -> Powder.AIR;
            case THUNDER -> Powder.EARTH;
            case WATER -> Powder.THUNDER;
            case FIRE -> Powder.WATER;
            case AIR -> Powder.FIRE;
        };
    }

    public static Powder getFromSymbol(char symbol) {
        return switch (symbol) {
            case '✤' -> EARTH;
            case '✦' -> THUNDER;
            case '❉' -> WATER;
            case '✹' -> FIRE;
            case '❋' -> AIR;
            default -> null;
        };
    }
}
