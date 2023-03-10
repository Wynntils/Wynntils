/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.wynntils.utils.StringUtils;
import java.util.Locale;
import net.minecraft.ChatFormatting;

public enum ShamanMaskType {
    NONE("None", ChatFormatting.GRAY, null),
    LUNATIC("L", ChatFormatting.RED, "§cL"),
    FANATIC("F", ChatFormatting.GOLD, "§6F"),
    COWARD("C", ChatFormatting.AQUA, "§bC"),
    AWAKENED("A", ChatFormatting.DARK_PURPLE, "Awakened");

    private final String alias;
    private final ChatFormatting color;
    private final String parseString;

    ShamanMaskType(String alias, ChatFormatting color, String parseString) {
        this.alias = alias;
        this.color = color;
        this.parseString = parseString;
    }

    public static ShamanMaskType find(String text) {
        for (ShamanMaskType type : values()) {
            if (type.alias.equals(text) || type.getName().equals(text)) {
                return type;
            }
        }

        return NONE;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public String getParseString() {
        return parseString;
    }

    public String getAlias() {
        return alias;
    }

    public String getName() {
        return StringUtils.capitalizeFirst(this.name().toLowerCase(Locale.ROOT));
    }
}
