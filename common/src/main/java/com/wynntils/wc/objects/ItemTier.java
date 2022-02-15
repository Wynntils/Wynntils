/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.objects;

import java.util.Arrays;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum ItemTier {
    Normal(ChatFormatting.WHITE),
    Unique(ChatFormatting.YELLOW),
    Rare(ChatFormatting.LIGHT_PURPLE),
    Set(ChatFormatting.GREEN),
    Fabled(ChatFormatting.RED),
    Legendary(ChatFormatting.AQUA),
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
        String name = component.getString();

        if (name.charAt(0) == '§') {
            return fromChatFormatting(ChatFormatting.getByCode(name.charAt(1)));
        }

        return null;
    }

    public static ItemTier fromChatFormatting(ChatFormatting formatting) {
        return Arrays.stream(ItemTier.values())
                .filter(t -> t.getChatFormatting() == formatting)
                .findFirst()
                .orElse(null);
    }
}
