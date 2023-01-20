/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.concepts;

import com.wynntils.mc.objects.CustomColor;
import com.wynntils.utils.StringUtils;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum Powder {
    EARTH('✤', Items.LIME_DYE, Items.GREEN_DYE, ChatFormatting.DARK_GREEN, ChatFormatting.GREEN),
    THUNDER('✦', Items.YELLOW_DYE, Items.ORANGE_DYE, ChatFormatting.YELLOW, ChatFormatting.GOLD),
    WATER('❉', Items.LIGHT_BLUE_DYE, Items.CYAN_DYE, ChatFormatting.AQUA, ChatFormatting.DARK_AQUA),
    FIRE('✹', Items.PINK_DYE, Items.RED_DYE, ChatFormatting.RED, ChatFormatting.DARK_RED),
    AIR('❋', Items.GRAY_DYE, Items.LIGHT_GRAY_DYE, ChatFormatting.WHITE, ChatFormatting.GRAY);

    private final char symbol;
    private final Item lowTierItem;
    private final Item highTierItem;
    private final ChatFormatting lightColor;
    private final ChatFormatting darkColor;

    Powder(char symbol, Item lowTierItem, Item highTierItem, ChatFormatting lightColor, ChatFormatting darkColor) {
        this.symbol = symbol;
        this.lowTierItem = lowTierItem;
        this.highTierItem = highTierItem;
        this.lightColor = lightColor;
        this.darkColor = darkColor;
    }

    public char getSymbol() {
        return symbol;
    }

    public CustomColor getColor() {
        return CustomColor.fromInt(this.lightColor.getColor()).withAlpha(255);
    }

    public String getColoredSymbol() {
        return lightColor.toString() + symbol;
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

    public Item getLowTierItem() {
        return lowTierItem;
    }

    public Item getHighTierItem() {
        return highTierItem;
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
