/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.elements.type;

import com.wynntils.utils.StringUtils;
import net.minecraft.ChatFormatting;

public enum Element {
    EARTH("✤", ChatFormatting.DARK_GREEN),
    THUNDER("✦", ChatFormatting.YELLOW),
    WATER("❉", ChatFormatting.AQUA),
    FIRE("✹", ChatFormatting.RED),
    AIR("❋", ChatFormatting.WHITE);

    private final String symbol;
    private final ChatFormatting colorCode;
    private final String displayName;

    Element(String symbol, ChatFormatting colorCode) {
        this.symbol = symbol;
        this.colorCode = colorCode;
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

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }

    public ChatFormatting getColorCode() {
        return colorCode;
    }
}
