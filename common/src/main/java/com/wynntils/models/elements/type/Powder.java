/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.elements.type;

import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CustomColor;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum Powder {
    EARTH(Element.EARTH, Items.LIME_DYE, Items.GREEN_DYE, ChatFormatting.DARK_GREEN, ChatFormatting.GREEN),
    THUNDER(Element.THUNDER, Items.YELLOW_DYE, Items.ORANGE_DYE, ChatFormatting.YELLOW, ChatFormatting.GOLD),
    WATER(Element.WATER, Items.LIGHT_BLUE_DYE, Items.CYAN_DYE, ChatFormatting.AQUA, ChatFormatting.DARK_AQUA),
    FIRE(Element.FIRE, Items.PINK_DYE, Items.RED_DYE, ChatFormatting.RED, ChatFormatting.DARK_RED),
    AIR(Element.AIR, Items.GRAY_DYE, Items.LIGHT_GRAY_DYE, ChatFormatting.WHITE, ChatFormatting.GRAY);

    private final Element element;
    private final Item lowTierItem;
    private final Item highTierItem;
    private final ChatFormatting lightColor;
    private final ChatFormatting darkColor;

    Powder(Element element, Item lowTierItem, Item highTierItem, ChatFormatting lightColor, ChatFormatting darkColor) {
        this.element = element;
        this.lowTierItem = lowTierItem;
        this.highTierItem = highTierItem;
        this.lightColor = lightColor;
        this.darkColor = darkColor;
    }

    public static Powder fromElement(Element element) {
        for (Powder powder : Powder.values()) {
            if (powder.element == element) {
                return powder;
            }
        }
        return null;
    }

    public static Powder getFromSymbol(String symbol) {
        Element element = Element.fromSymbol(symbol);
        return fromElement(element);
    }

    public Element getElement() {
        return element;
    }

    public char getSymbol() {
        return element.getSymbol().charAt(0);
    }

    public CustomColor getColor() {
        return CustomColor.fromInt(this.lightColor.getColor()).withAlpha(255);
    }

    public String getColoredSymbol() {
        return lightColor.toString() + getSymbol();
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
}
