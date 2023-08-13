/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.wynntils.core.text.StyledText;
import com.wynntils.utils.StringUtils;
import java.util.Locale;
import net.minecraft.ChatFormatting;

public enum ShamanMaskType {
    NONE("None", ChatFormatting.GRAY, null),
    LUNATIC("L", ChatFormatting.RED, StyledText.fromString("§cL")),
    FANATIC("F", ChatFormatting.GOLD, StyledText.fromString("§6F")),
    COWARD("C", ChatFormatting.AQUA, StyledText.fromString("§bC")),
    AWAKENED("A", ChatFormatting.DARK_PURPLE, StyledText.fromString("Awakened"));

    private final String alias;
    private final ChatFormatting color;
    private final StyledText parseString;

    ShamanMaskType(String alias, ChatFormatting color, StyledText parseString) {
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

    public StyledText getParseString() {
        return parseString;
    }

    public String getAlias() {
        return alias;
    }

    public String getName() {
        return StringUtils.capitalizeFirst(this.name().toLowerCase(Locale.ROOT));
    }
}
