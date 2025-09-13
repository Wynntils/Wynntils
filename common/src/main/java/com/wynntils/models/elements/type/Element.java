/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.elements.type;

import com.mojang.serialization.Codec;
import com.wynntils.utils.StringUtils;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.util.StringRepresentable;

public enum Element implements StringRepresentable {
    EARTH("\uE001", ChatFormatting.DARK_GREEN, 0),
    THUNDER("\uE003", ChatFormatting.YELLOW, 1),
    WATER("\uE004", ChatFormatting.AQUA, 2),
    FIRE("\uE002", ChatFormatting.RED, 3),
    AIR("\uE000", ChatFormatting.WHITE, 4);

    public static final Codec<Element> CODEC = StringRepresentable.fromEnum(Element::values);

    private final String symbol;
    private final ChatFormatting colorCode;
    private final String displayName;
    private final int encodingId;

    Element(String symbol, ChatFormatting colorCode, int encodingId) {
        this.symbol = symbol;
        this.colorCode = colorCode;
        this.encodingId = encodingId;
        this.displayName = StringUtils.capitalized(this.name());
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Element fromSymbol(String symbol) {
        for (Element element : Element.values()) {
            if (element.symbol.equals(symbol)) {
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

    public ChatFormatting getColorCode() {
        return colorCode;
    }

    public int getEncodingId() {
        return encodingId;
    }
}
