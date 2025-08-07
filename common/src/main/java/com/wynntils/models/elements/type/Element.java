/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.elements.type;

import com.wynntils.utils.StringUtils;
import net.minecraft.ChatFormatting;

public enum Element {
    EARTH("\uE001", "✤", ChatFormatting.DARK_GREEN, 0),
    THUNDER("\uE003", "✦", ChatFormatting.YELLOW, 1),
    WATER("\uE004", "❉", ChatFormatting.AQUA, 2),
    FIRE("\uE002", "✹", ChatFormatting.RED, 3),
    AIR("\uE000", "❋", ChatFormatting.WHITE, 4);

    private final String parseSymbol;
    private final String displaySymbol;
    private final ChatFormatting colorCode;
    private final String displayName;
    private final int encodingId;

    Element(String parseSymbol, String displaySymbol, ChatFormatting colorCode, int encodingId) {
        this.parseSymbol = parseSymbol;
        this.displaySymbol = displaySymbol;
        this.colorCode = colorCode;
        this.encodingId = encodingId;
        this.displayName = StringUtils.capitalized(this.name());
    }

    public static Element fromSymbol(String symbol) {
        for (Element element : Element.values()) {
            if (element.parseSymbol.equals(symbol)) {
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

    public String getParseSymbol() {
        return parseSymbol;
    }

    public String getDisplaySymbol() {
        return displaySymbol;
    }

    public ChatFormatting getColorCode() {
        return colorCode;
    }

    public int getEncodingId() {
        return encodingId;
    }
}
