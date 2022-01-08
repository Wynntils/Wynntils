/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.objects;

import com.wynntils.mc.utils.StringUtils;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

public enum ItemTier {
    Normal(ChatFormatting.WHITE),
    Unique(ChatFormatting.YELLOW),
    Rare(ChatFormatting.LIGHT_PURPLE),
    Set(ChatFormatting.GREEN),
    Fabled(ChatFormatting.RED),
    Legendary(ChatFormatting.BLUE),
    Mythic(ChatFormatting.DARK_PURPLE),
    Crafted(ChatFormatting.DARK_AQUA);

    ChatFormatting chatFormatting;

    ItemTier(ChatFormatting chatFormatting) {
        this.chatFormatting = chatFormatting;
    }

    public ChatFormatting getChatFormatting() {
        return chatFormatting;
    }

    public static ItemTier fromComponent(Component component) {
        TextColor color = component.getStyle().getColor();
        if (color == null) return null;
        Optional<ChatFormatting> formatting = StringUtils.getChatFormatting(color);
        return formatting.map(ItemTier::fromChatFormatting).orElse(null);
    }

    public static ItemTier fromChatFormatting(ChatFormatting formatting) {
        return Arrays.stream(ItemTier.values())
                .filter(t -> t.getChatFormatting() == formatting)
                .findFirst()
                .orElse(null);
    }
}
