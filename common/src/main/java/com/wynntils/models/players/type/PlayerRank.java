/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.type;

import net.minecraft.ChatFormatting;

public enum PlayerRank {
    NONE("", "", ChatFormatting.DARK_GRAY),
    // normal ranks
    VIP("VIP", "\uE023", ChatFormatting.DARK_GREEN),
    VIP_PLUS("VIP+", "\uE024", ChatFormatting.BLUE),
    HERO("HERO", "\uE01B", ChatFormatting.DARK_PURPLE),
    HERO_PLUS("HERO+", "\uE08A", ChatFormatting.LIGHT_PURPLE),
    CHAMPION("CHAMPION", "\uE017", ChatFormatting.YELLOW),
    MEDIA("Media", "\uE01E", ChatFormatting.LIGHT_PURPLE),
    // ct ranks (all have same colors)
    ARTIST("Artist", "\uE015", ChatFormatting.AQUA),
    BUILDER("Builder", "\uE016", ChatFormatting.AQUA),
    CMD("CMD", "\uE018", ChatFormatting.AQUA),
    GM("GM", "\uE01A", ChatFormatting.AQUA),
    HYBRID("Hybrid", "\uE01C", ChatFormatting.AQUA),
    ITEM("Item", "\uE01D", ChatFormatting.AQUA),
    MUSIC("Music", "\uE020", ChatFormatting.AQUA),
    QA("QA", "\uE022", ChatFormatting.AQUA),
    // staff ranks
    DEV("Dev", "\uE019", ChatFormatting.RED),
    MOD("Mod", "\uE01F", ChatFormatting.GOLD),
    ADMIN("Admin", "\uE014", ChatFormatting.RED),
    OWNER("Owner", "\uE021", ChatFormatting.DARK_RED),
    WEB("Web", "\uE025", ChatFormatting.RED);

    private final String name;
    private final String tag;
    private final ChatFormatting textColor;

    PlayerRank(String name, String tag, ChatFormatting textColor) {
        this.name = name;
        this.tag = tag;
        this.textColor = textColor;
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
}
