/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.type;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum AccountType {
    NORMAL(null),
    BANNED(null),
    DONATOR(Component.literal("Wynntils Donator").withStyle(ChatFormatting.LIGHT_PURPLE)),
    CONTENT_TEAM(Component.literal("Wynntils CT").withStyle(ChatFormatting.DARK_AQUA)),
    HELPER(Component.literal("Wynntils Helper").withStyle(ChatFormatting.GREEN)),
    MODERATOR(Component.literal("Wynntils Developer")
            .withStyle(ChatFormatting.GOLD)
            .withStyle(ChatFormatting.BOLD));

    private final MutableComponent component;

    AccountType(MutableComponent component) {
        this.component = component;
    }

    public MutableComponent getComponent() {
        return component;
    }
}
