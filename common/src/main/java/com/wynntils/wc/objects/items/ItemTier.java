/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.objects.items;

import com.google.gson.annotations.SerializedName;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.Arrays;

public enum ItemTier {
    @SerializedName("NORMAL")
    Normal(ChatFormatting.WHITE, -1, 0),
    @SerializedName("UNIQUE")
    Unique(ChatFormatting.YELLOW, 3, 0.5f),
    @SerializedName("RARE")
    Rare(ChatFormatting.LIGHT_PURPLE, 8, 1.2f),
    @SerializedName("SET")
    Set(ChatFormatting.GREEN, 8, 1.2f),
    @SerializedName("FABLED")
    Fabled(ChatFormatting.RED, 12, 4.5f),
    @SerializedName("LEGENDARY")
    Legendary(ChatFormatting.AQUA, 16, 8.0f),
    @SerializedName("MYTHIC")
    Mythic(ChatFormatting.DARK_PURPLE, 90, 18.0f),
    @SerializedName("CRAFTED")
    Crafted(ChatFormatting.DARK_AQUA, -1, 0);

    private final ChatFormatting chatFormatting;
    private final int baseCost;
    private final float costMultiplier;

    ItemTier(ChatFormatting chatFormatting, int baseCost, float costMultiplier) {
        this.chatFormatting = chatFormatting;
        this.baseCost = baseCost;
        this.costMultiplier = costMultiplier;
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

    public int getItemIdentificationCost(int level) {
        return this.baseCost + (int) Math.ceil(level * this.costMultiplier);
    }
}
