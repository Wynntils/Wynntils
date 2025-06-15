/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.type;

import com.wynntils.utils.mc.ComponentUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum AccountType {
    NORMAL(null),
    BANNED(null),
    DONATOR(Component.literal("Wynntils Donator")),
    CONTENT_TEAM(Component.literal("Wynntils CT").withStyle(ChatFormatting.DARK_AQUA)),
    TRANSLATOR(Component.literal("Wynntils Translator").withStyle(ChatFormatting.DARK_AQUA)),
    HELPER(Component.literal("Wynntils Helper").withStyle(ChatFormatting.GREEN)),
    SUPPORT(Component.literal("Wynntils Support").withStyle(ChatFormatting.GREEN)),
    MODERATOR(Component.literal("Wynntils Moderator").withStyle(ChatFormatting.BLUE)),
    DEVELOPER(Component.literal("Wynntils Developer")
            .withStyle(ChatFormatting.GOLD)
            .withStyle(ChatFormatting.BOLD)),
    ADMIN(Component.literal("Wynntils Admin").withStyle(ChatFormatting.AQUA).withStyle(ChatFormatting.BOLD));

    private final MutableComponent component;

    AccountType(MutableComponent component) {
        this.component = component;
    }

    public MutableComponent getComponent() {
        if (this == DONATOR) {
            return ComponentUtils.makeRainbowStyle(component.getString(), false);
        }

        // FIXME: Remove when Athena account types are properly implemented
        if (this == MODERATOR) {
            return DEVELOPER.getComponent();
        }

        return component;
    }
}
