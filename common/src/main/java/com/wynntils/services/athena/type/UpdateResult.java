/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.athena.type;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum UpdateResult {
    SUCCESSFUL(
            Component.translatable("service.wynntils.updates.result.successful").withStyle(ChatFormatting.DARK_GREEN)),
    ALREADY_ON_LATEST(
            Component.translatable("service.wynntils.updates.result.latest").withStyle(ChatFormatting.YELLOW)),
    UPDATE_PENDING(
            Component.translatable("service.wynntils.updates.result.pending").withStyle(ChatFormatting.YELLOW)),
    INCORRECT_VERSION_RECEIVED(
            Component.translatable("service.wynntils.updates.result.incorrect").withStyle(ChatFormatting.YELLOW)),
    ERROR(Component.translatable("service.wynntils.updates.result.error").withStyle(ChatFormatting.DARK_RED));

    private final MutableComponent message;

    UpdateResult(MutableComponent message) {
        this.message = message;
    }

    public MutableComponent getMessage() {
        return message;
    }
}
