/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.type;

import net.minecraft.ChatFormatting;

public enum PlayerRank {
    NONE("", ' ', ChatFormatting.DARK_GRAY, ChatFormatting.DARK_GRAY),
    VIP("VIP", '\uE023', ChatFormatting.DARK_GREEN, ChatFormatting.GREEN),
    VIP_PLUS("VIP+", '\uE024', ChatFormatting.DARK_AQUA, ChatFormatting.AQUA),
    HERO("HERO", '\uE01B', ChatFormatting.DARK_PURPLE, ChatFormatting.LIGHT_PURPLE),
    CHAMPION("CHAMPION", '\uE017', ChatFormatting.YELLOW, ChatFormatting.GOLD),
    MOD("Mod", ' ', ChatFormatting.GOLD, ChatFormatting.YELLOW),
    YT("YT", ChatFormatting.LIGHT_PURPLE, ChatFormatting.DARK_PURPLE),
    BUILD("Build", ChatFormatting.AQUA, ChatFormatting.DARK_AQUA),
    ART("Art", ChatFormatting.AQUA, ChatFormatting.DARK_AQUA),
    ITEM("Item", ChatFormatting.AQUA, ChatFormatting.DARK_AQUA),
    ADMIN("Admin", ChatFormatting.RED, ChatFormatting.DARK_RED);
    char cmd = '\uE018';
    char dev = '\uE019';
    char music = '\uE020';
    char owner = '\uE021';
    char builder = '\uE016';
    char artist = '\uE015';
    char admin = '\uE014';
    char qa = '\uE022';
    char web = '\uE025';

    String teststring = "\uE014 \uE015 \uE016 \uE017 \uE018 \uE019 \uE01A \uE01B \uE01C \uE01D \uE01E \uE01F \uE020 \uE021 \uE022 \uE023 \uE024 \uE025";
    private final String tag;
    private final char newTag;
    private final ChatFormatting primaryColor;
    private final ChatFormatting secondaryColor;

    PlayerRank(String tag, char newTag, ChatFormatting primaryColor, ChatFormatting secondaryColor) {
        this.tag = tag;
        this.newTag = newTag;
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
