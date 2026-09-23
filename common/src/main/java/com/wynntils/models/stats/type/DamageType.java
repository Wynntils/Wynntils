/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.stats.type;

import com.wynntils.models.elements.type.Element;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.TextColor;

public enum DamageType {
    ALL("", "❤", TextColor.DARK_RED),
    NEUTRAL("Neutral", "\uE005", TextColor.GOLD, 5),
    FIRE(Element.FIRE),
    WATER(Element.WATER),
    AIR(Element.AIR),
    THUNDER(Element.THUNDER),
    EARTH(Element.EARTH),
    RAINBOW("Elemental"),
    POISON("Poison", "☠", TextColor.DARK_PURPLE);

    private final Element element;
    private final String displayName;
    private final String apiName;
    private final String symbol;
    private final String tooltipSprite;
    private final TextColor textColor;
    private final int encodingId;

    DamageType(String name) {
        this.element = null;
        // displayName needs padding if non-empty
        this.displayName = name.isEmpty() ? "" : name + " ";
        this.apiName = name;
        this.symbol = "";
        this.tooltipSprite = "";
        this.textColor = null;
        this.encodingId = -1;
    }

    DamageType(String name, String symbol, TextColor textColor) {
        this.element = null;
        // displayName needs padding if non-empty
        this.displayName = name.isEmpty() ? "" : name + " ";
        this.apiName = name;

        this.symbol = symbol;
        this.tooltipSprite = "";
        this.textColor = textColor;
        this.encodingId = -1;
    }

    DamageType(String name, String symbol, TextColor textColor, int encodingId) {
        this.element = null;
        // displayName needs padding if non-empty
        this.displayName = name.isEmpty() ? "" : name + " ";
        this.apiName = name;

        this.symbol = symbol;
        this.tooltipSprite = symbol;
        this.textColor = textColor;
        this.encodingId = encodingId;
    }

    DamageType(Element element) {
        this.element = element;
        // displayName needs padding
        this.displayName = element.getDisplayName() + " ";
        this.apiName = element.getDisplayName();
        this.symbol = element.getSymbol();
        this.tooltipSprite = element.getTooltipSprite();
        this.textColor = element.getTextColor();

        // Encoding id is the element id
        this.encodingId = element.getEncodingId();
    }

    public static List<DamageType> statValues() {
        // Poison is only used in damage labels, not stats
        return Arrays.stream(values()).filter(type -> type != POISON).toList();
    }

    public static DamageType fromElement(Element element) {
        for (DamageType type : values()) {
            if (type.element == element) return type;
        }
        return null;
    }

    public static DamageType fromSymbol(String symbol) {
        for (DamageType type : values()) {
            if (type.symbol.equals(symbol)) return type;
        }
        return null;
    }

    public static DamageType fromTooltipSprite(String sprite) {
        for (DamageType type : values()) {
            if (type.tooltipSprite.equals(sprite)) return type;
        }
        return null;
    }

    public static DamageType fromEncodingId(int id) {
        for (DamageType type : values()) {
            if (type.encodingId == id) {
                return type;
            }
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

    public String getTooltipSprite() {
        return tooltipSprite;
    }

    public TextColor getTextColor() {
        return textColor;
    }

    public int getEncodingId() {
        assert encodingId != -1 : "Encoding id not set for " + this;
        return encodingId;
    }
}
