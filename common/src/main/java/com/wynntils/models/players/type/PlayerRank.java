/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.type;

import net.minecraft.ChatFormatting;

public enum PlayerRank {
    NONE("", ChatFormatting.DARK_GRAY, ChatFormatting.DARK_GRAY),
    VIP("VIP", ChatFormatting.DARK_GREEN, ChatFormatting.GREEN),
    VIP_PLUS("VIP+", ChatFormatting.DARK_AQUA, ChatFormatting.AQUA),
    HERO("HERO", ChatFormatting.DARK_PURPLE, ChatFormatting.LIGHT_PURPLE),
    CHAMPION("CHAMPION", ChatFormatting.YELLOW, ChatFormatting.GOLD),
    MOD("Mod", ChatFormatting.GOLD, ChatFormatting.YELLOW),
    YT("YT", ChatFormatting.LIGHT_PURPLE, ChatFormatting.DARK_PURPLE),
    BUILD("Build", ChatFormatting.AQUA, ChatFormatting.DARK_AQUA),
    ART("Art", ChatFormatting.AQUA, ChatFormatting.DARK_AQUA),
    ITEM("Item", ChatFormatting.AQUA, ChatFormatting.DARK_AQUA),
    ADMIN("Admin", ChatFormatting.RED, ChatFormatting.DARK_RED);

    private final String tag;
    private final ChatFormatting primaryColor;
    private final ChatFormatting secondaryColor;

    PlayerRank(String tag, ChatFormatting primaryColor, ChatFormatting secondaryColor) {
        this.tag = tag;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
    }

    public static PlayerRank fromString(String rankString) {
        for (PlayerRank rank : values()) {
            if (rank.tag.equals(rankString)) {
                return rank;
            }
        }

        return NONE;
    }

    public String getFormattedRank() {
        return primaryColor + "[" + secondaryColor + tag + primaryColor + "] " + secondaryColor;
    }

    public String getTag() {
        return tag;
    }

    public ChatFormatting getPrimaryColor() {
        return primaryColor;
    }

    public ChatFormatting getSecondaryColor() {
        return secondaryColor;
    }
}
