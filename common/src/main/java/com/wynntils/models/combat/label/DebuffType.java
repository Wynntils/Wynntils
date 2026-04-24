/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.combat.label;

import com.wynntils.utils.EnumUtils;
import com.wynntils.utils.colors.CustomColor;
import net.minecraft.ChatFormatting;

public enum DebuffType {
    BLEEDING('\uE031', ChatFormatting.RED),
    BLINDNESS('\u2B23', ChatFormatting.RED),
    BURNING('\u2739', ChatFormatting.RED),
    CONFUSED('\uE03C', CustomColor.fromInt(0xE1DCA4)),
    CONTAMINATED('\uE043', CustomColor.fromInt(0x94A771)),
    CRYSTALLIZED('\uE045', CustomColor.fromInt(0xB6FBFC)),
    CURSE('\u2749', ChatFormatting.RED),
    DISCOMBOBULATED('\u2699', ChatFormatting.YELLOW),
    ENKINDLED('\uE03D', CustomColor.fromInt(0xFF8E8E)),
    FREEZING('\u2744', ChatFormatting.AQUA),
    MARKED('\u271C', ChatFormatting.RED),
    POISON('\u2620', ChatFormatting.DARK_PURPLE),
    PROVOKED('\uE025', ChatFormatting.RED),
    RESISTANCE('\uE015', ChatFormatting.RED),
    SLOWNESS('\u2B24', ChatFormatting.RED),
    TRICK('\uE03A', CustomColor.fromInt(0x6AFA65)),
    TWILIGHT('\uE04B', CustomColor.fromInt(0x5B88FC)),
    WEAKNESS('\u2694', ChatFormatting.RED),
    WHIPPED('\u21F6', ChatFormatting.GOLD),
    WIND_PRISON('\u274B', ChatFormatting.RED);

    private final char symbol;
    private final CustomColor color;
    private final String friendlyName;

    DebuffType(char symbol, ChatFormatting color) {
        this(symbol, CustomColor.fromChatFormatting(color));
    }

    DebuffType(char symbol, CustomColor color) {
        this.symbol = symbol;
        this.color = color;
        this.friendlyName = EnumUtils.toNiceString(name());
    }

    public char symbol() {
        return symbol;
    }

    public CustomColor color() {
        return color;
    }

    public String friendlyName() {
        return friendlyName;
    }

    public static DebuffType fromSymbol(char symbol) {
        for (DebuffType type : values()) {
            if (type.symbol() == symbol) {
                return type;
            }
        }

        return null;
    }

    public static DebuffType fromName(String name) {
        for (DebuffType type : values()) {
            if (type.friendlyName().equalsIgnoreCase(name)) {
                return type;
            }
        }

        return null;
    }
}
