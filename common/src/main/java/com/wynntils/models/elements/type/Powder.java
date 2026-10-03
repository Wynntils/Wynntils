/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.elements.type;

import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CustomColor;
import java.util.Locale;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum Powder {
    EARTH(Element.EARTH, Items.DYE.lime(), Items.DYE.green(), TextColor.DARK_GREEN, TextColor.GREEN, "Quake"),
    THUNDER(
            Element.THUNDER,
            Items.DYE.yellow(),
            Items.DYE.orange(),
            TextColor.YELLOW,
            TextColor.GOLD,
            "Chain Lightning"),
    WATER(Element.WATER, Items.DYE.lightBlue(), Items.DYE.cyan(), TextColor.AQUA, TextColor.DARK_AQUA, "Curse"),
    FIRE(Element.FIRE, Items.DYE.pink(), Items.DYE.red(), TextColor.RED, TextColor.DARK_RED, "Courage"),
    AIR(Element.AIR, Items.DYE.gray(), Items.DYE.lightGray(), TextColor.WHITE, TextColor.GRAY, "Wind Prison");

    private final Element element;
    private final Item lowTierItem;
    private final Item highTierItem;
    private final TextColor lightColor;
    private final TextColor darkColor;
    private final String specialName;

    Powder(
            Element element,
            Item lowTierItem,
            Item highTierItem,
            TextColor lightColor,
            TextColor darkColor,
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
        return CustomColor.fromTextColor(this.lightColor).withAlpha(255);
    }

    public Item getLowTierItem() {
        return lowTierItem;
    }

    public Item getHighTierItem() {
        return highTierItem;
    }

    public TextColor getLightColor() {
        return lightColor;
    }

    public TextColor getDarkColor() {
        return darkColor;
    }

    public String getSpecialName() {
        return specialName;
    }

    public String getName() {
        return StringUtils.capitalizeFirst(this.name().toLowerCase(Locale.ROOT));
    }
}
