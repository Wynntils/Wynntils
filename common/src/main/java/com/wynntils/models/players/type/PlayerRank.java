/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.type;

import net.minecraft.ChatFormatting;

public enum PlayerRank {
    NONE("", "", ChatFormatting.DARK_GRAY, ChatFormatting.DARK_GRAY),
    // normal ranks
    VIP("VIP", "\uE023", ChatFormatting.DARK_GREEN, ChatFormatting.GREEN),
    VIP_PLUS("VIP+", "\uE024", ChatFormatting.BLUE, ChatFormatting.DARK_AQUA),
    HERO("HERO", "\uE01B", ChatFormatting.DARK_PURPLE, ChatFormatting.LIGHT_PURPLE),
    CHAMPION("CHAMPION", "\uE017", ChatFormatting.YELLOW, ChatFormatting.GOLD),
    MEDIA("Media", "\uE01E", ChatFormatting.LIGHT_PURPLE, ChatFormatting.DARK_PURPLE),
    // ct ranks (all have same colors)
    ARTIST("Artist", "\uE015", ChatFormatting.AQUA, ChatFormatting.DARK_AQUA),
    BUILDER("Builder", "\uE016", ChatFormatting.AQUA, ChatFormatting.DARK_AQUA),
    CMD("CMD", "\uE018", ChatFormatting.AQUA, ChatFormatting.DARK_AQUA),
    GM("GM", "\uE01A", ChatFormatting.AQUA, ChatFormatting.DARK_AQUA),
    HYBRID("Hybrid", "\uE01C", ChatFormatting.AQUA, ChatFormatting.DARK_AQUA),
    ITEM("Item", "\uE01D", ChatFormatting.AQUA, ChatFormatting.DARK_AQUA),
    MUSIC("Music", "\uE020", ChatFormatting.AQUA, ChatFormatting.DARK_AQUA),
    QA("QA", "\uE022", ChatFormatting.AQUA, ChatFormatting.DARK_AQUA),
    // staff ranks
    DEV("Dev", "\uE019", ChatFormatting.RED, ChatFormatting.DARK_RED),
    MOD("Mod", "\uE01F", ChatFormatting.GOLD, ChatFormatting.GOLD),
    ADMIN("Admin", "\uE014", ChatFormatting.RED, ChatFormatting.DARK_RED),
    OWNER("Owner", "\uE021", ChatFormatting.DARK_RED, ChatFormatting.DARK_RED),
    WEB("Web", "\uE025", ChatFormatting.RED, ChatFormatting.DARK_RED);

    private final String name;
    private final String tag;
    private final ChatFormatting textColor;
    private final ChatFormatting accentColor;

    PlayerRank(String name, String tag, ChatFormatting textColor, ChatFormatting accentColor) {
        this.name = name;
        this.tag = tag;
        this.textColor = textColor;
        this.accentColor = accentColor;
    }

    public static PlayerRank fromString(String rankString) {
        for (PlayerRank rank : values()) {
            if (rank.name.equals(rankString) || rank.tag.equals(rankString)) {
                return rank;
            }
        }

        return NONE;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public ChatFormatting getTextColor() {
        return textColor;
    }

    public ChatFormatting getAccentColor() {
        return accentColor;
    }
}
