/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.gear.types;

import com.wynntils.models.spells.type.Element;
import java.util.Optional;
import net.minecraft.ChatFormatting;

public enum GearDamageType {
    ANY(""),
    NEUTRAL("Neutral", "✣", ChatFormatting.GOLD),
    RAINBOW("Elemental"),
    AIR(Element.AIR),
    EARTH(Element.EARTH),
    FIRE(Element.FIRE),
    THUNDER(Element.THUNDER),
    WATER(Element.WATER);

    private final Element element;
    private final String displayName;
    private final String apiName;
    private final String symbol;
    private final ChatFormatting colorCode;

    GearDamageType(String name) {
        this.element = null;
        // displayName needs padding if non-empty
        this.displayName = name.isEmpty() ? "" : name + " ";
        this.apiName = name;
        this.symbol = "";
        this.colorCode = null;
    }

    GearDamageType(String name, String symbol, ChatFormatting colorCode) {
        this.element = null;
        // displayName needs padding if non-empty
        this.displayName = name.isEmpty() ? "" : name + " ";
        this.apiName = name;

        this.symbol = symbol;
        this.colorCode = colorCode;
    }

    GearDamageType(Element element) {
        this.element = element;
        // displayName needs padding
        this.displayName = element.getDisplayName() + " ";
        this.apiName = element.getDisplayName();
        this.symbol = element.getSymbol();
        this.colorCode = element.getColorCode();
    }

    public static GearDamageType fromElement(Element element) {
        for (GearDamageType type : values()) {
            if (type.element == element) return type;
        }
        return null;
    }

    public Optional<Element> getElement() {
        return Optional.ofNullable(element);
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getApiName() {
        return apiName;
    }

    public String getSymbol() {
        return symbol;
    }

    public ChatFormatting getColorCode() {
        return colorCode;
    }
}
