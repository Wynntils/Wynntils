/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects.account;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public enum AccountType {
    NORMAL(null),
    BANNED(null),
    DONATOR(new TextComponent("Wynntils Donator").withStyle(ChatFormatting.LIGHT_PURPLE)),
    CONTENT_TEAM(new TextComponent("Wynntils CT").withStyle(ChatFormatting.DARK_AQUA)),
    HELPER(new TextComponent("Wynntils Helper").withStyle(ChatFormatting.GREEN)),
    MODERATOR(new TextComponent("Wynntils Developer")
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
