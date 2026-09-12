/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import com.wynntils.core.text.StyledText;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import java.util.Locale;

public enum ShamanMaskType {
    NONE("None", CommonColors.GRAY, null, null),
    LUNATIC("L", CustomColor.fromInt(0xf4557d), "\uE024"),
    HERETIC("H", CustomColor.fromInt(0x99e9ff), "\uE022"),
    FANATIC("F", CustomColor.fromInt(0xffc251), "\uE023"),
    AWAKENED("A", CommonColors.WHITE, "Awakened", StyledText.fromString("§fAwakened"));

    private final String alias;
    private final CustomColor color;
    private final String maskDisplayString;
    private final StyledText parseStyledText;

    ShamanMaskType(String alias, CustomColor color, String maskDisplayString, StyledText parseStyledText) {
        this.alias = alias;
        this.color = color;
        this.maskDisplayString = maskDisplayString;
        this.parseStyledText = parseStyledText;
    }

    ShamanMaskType(String alias, CustomColor color, String maskDisplayString) {
        this.alias = alias;
        this.color = color;
        this.maskDisplayString = maskDisplayString;
        this.parseStyledText = StyledText.fromString("§" + color.toHexString() + maskDisplayString);
    }

    public static ShamanMaskType find(String text) {
        for (ShamanMaskType type : values()) {
            if (type.alias.equalsIgnoreCase(text) || type.getName().equalsIgnoreCase(text)) {
                return type;
            }
        }

        return NONE;
    }

    public CustomColor getColor() {
        return color;
    }

    public String getMaskDisplayString() {
        return maskDisplayString;
    }

    public StyledText getParseStyledText() {
        return parseStyledText;
    }

    public String getAlias() {
        return alias;
    }

    public String getName() {
        return StringUtils.capitalizeFirst(this.name().toLowerCase(Locale.ROOT));
    }
}
