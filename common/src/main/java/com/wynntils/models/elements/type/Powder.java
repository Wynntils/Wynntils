/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.elements.type;

import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CustomColor;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum Powder {
    EARTH(Element.EARTH, Items.LIME_DYE, Items.GREEN_DYE, ChatFormatting.DARK_GREEN, ChatFormatting.GREEN, "Quake"),
    THUNDER(
            Element.THUNDER,
            Items.YELLOW_DYE,
            Items.ORANGE_DYE,
            ChatFormatting.YELLOW,
            ChatFormatting.GOLD,
            "Chain Lightning"),
    WATER(Element.WATER, Items.LIGHT_BLUE_DYE, Items.CYAN_DYE, ChatFormatting.AQUA, ChatFormatting.DARK_AQUA, "Curse"),
    FIRE(Element.FIRE, Items.PINK_DYE, Items.RED_DYE, ChatFormatting.RED, ChatFormatting.DARK_RED, "Courage"),
    AIR(Element.AIR, Items.GRAY_DYE, Items.LIGHT_GRAY_DYE, ChatFormatting.WHITE, ChatFormatting.GRAY, "Wind Prison");

    private final Element element;
    private final Item lowTierItem;
    private final Item highTierItem;
    private final ChatFormatting lightColor;
    private final ChatFormatting darkColor;
    private final String specialName;

    Powder(
            Element element,
            Item lowTierItem,
            Item highTierItem,
            ChatFormatting lightColor,
            ChatFormatting darkColor,
            String specialName) {
        this.element = element;
        this.lowTierItem = lowTierItem;
        this.highTierItem = highTierItem;
        this.lightColor = lightColor;
        this.darkColor = darkColor;
        this.specialName = specialName;
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

    public String getSpecialName() {
        return specialName;
    }

    public String getName() {
        return StringUtils.capitalizeFirst(this.name().toLowerCase(Locale.ROOT));
    }
}
