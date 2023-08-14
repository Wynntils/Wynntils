/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.colors;

import net.minecraft.ChatFormatting;

/**
 * This enum is used for configs that need a ChatFormatting color,
 * but does not want to allow other types of formatting.
 */
public enum ColorChatFormatting {
    BLACK(ChatFormatting.BLACK),
    DARK_BLUE(ChatFormatting.DARK_BLUE),
    DARK_GREEN(ChatFormatting.DARK_GREEN),
    DARK_AQUA(ChatFormatting.DARK_AQUA),
    DARK_RED(ChatFormatting.DARK_RED),
    DARK_PURPLE(ChatFormatting.DARK_PURPLE),
    GOLD(ChatFormatting.GOLD),
    GRAY(ChatFormatting.GRAY),
    DARK_GRAY(ChatFormatting.DARK_GRAY),
    BLUE(ChatFormatting.BLUE),
    GREEN(ChatFormatting.GREEN),
    AQUA(ChatFormatting.AQUA),
    RED(ChatFormatting.RED),
    LIGHT_PURPLE(ChatFormatting.LIGHT_PURPLE),
    YELLOW(ChatFormatting.YELLOW),
    WHITE(ChatFormatting.WHITE);

    private final ChatFormatting chatFormatting;

    ColorChatFormatting(ChatFormatting chatFormatting) {
        this.chatFormatting = chatFormatting;
    }

    public ChatFormatting getChatFormatting() {
        return chatFormatting;
    }
}
