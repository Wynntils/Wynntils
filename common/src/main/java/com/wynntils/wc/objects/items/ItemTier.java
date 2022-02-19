/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.objects.items;

import java.util.Arrays;

import com.google.gson.annotations.SerializedName;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum ItemTier {
    @SerializedName("NORMAL")
    Normal(ChatFormatting.WHITE),
    @SerializedName("UNIQUE")
    Unique(ChatFormatting.YELLOW),
    @SerializedName("RARE")
    Rare(ChatFormatting.LIGHT_PURPLE),
    @SerializedName("SET")
    Set(ChatFormatting.GREEN),
    @SerializedName("FABLED")
    Fabled(ChatFormatting.RED),
    @SerializedName("LEGENDARY")
    Legendary(ChatFormatting.AQUA),
    @SerializedName("MYTHIC")
    Mythic(ChatFormatting.DARK_PURPLE),
    @SerializedName("CRAFTED")
    Crafted(ChatFormatting.DARK_AQUA);

    final ChatFormatting chatFormatting;

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
