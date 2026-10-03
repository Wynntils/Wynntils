/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.elements.type;

import com.wynntils.utils.StringUtils;
import net.minecraft.network.chat.TextColor;

public enum Element {
    EARTH("\uE001", "\uE000", TextColor.DARK_GREEN, 0),
    THUNDER("\uE003", "\uE001", TextColor.YELLOW, 1),
    WATER("\uE004", "\uE002", TextColor.AQUA, 2),
    FIRE("\uE002", "\uE003", TextColor.RED, 3),
    AIR("\uE000", "\uE004", TextColor.WHITE, 4);

    private final String symbol;
    private final String tooltipSprite;
    private final TextColor textColor;
    private final String displayName;
    private final int encodingId;

    Element(String symbol, String tooltipSprite, TextColor textColor, int encodingId) {
        this.symbol = symbol;
        this.tooltipSprite = tooltipSprite;
        this.textColor = textColor;
        this.encodingId = encodingId;
        this.displayName = StringUtils.capitalized(this.name());
    }

    public static Element fromSymbol(String symbol) {
        for (Element element : Element.values()) {
            if (element.symbol.equals(symbol)) {
                return element;
            }
        }
        return null;
    }

    public static Element fromTooltipSprite(String sprite) {
        for (Element element : Element.values()) {
            if (element.tooltipSprite.equals(sprite)) {
                return element;
            }
        }
        return null;
    }

    public static Element fromEncodingId(int encodingId) {
        for (Element element : Element.values()) {
            if (element.encodingId == encodingId) {
                return element;
            }
        }
        return null;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }

    public TextColor getTextColor() {
        return textColor;
    }

    public String getTooltipSprite() {
        return tooltipSprite;
    }

    public int getEncodingId() {
        return encodingId;
    }
}
