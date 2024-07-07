/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.type;

import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum ActivityStatus {
    STARTED(Pattern.compile(ChatFormatting.GREEN + "Currently in progress")),
    AVAILABLE(Pattern.compile(ChatFormatting.YELLOW + "Can be .+")),
    UNAVAILABLE(Pattern.compile(ChatFormatting.RED + "Cannot be .+")),
    COMPLETED(Pattern.compile(ChatFormatting.GREEN + "Already completed"));

    private final Pattern statusPattern;

    ActivityStatus(Pattern statusMessage) {
        this.statusPattern = statusMessage;
    }

    public static ActivityStatus from(String statusLine) {
        for (ActivityStatus status : values()) {
            if (status.statusPattern.matcher(statusLine).matches()) return status;
        }

        return null;
    }

    public Component getQuestStateComponent() {
        return switch (this) {
            case STARTED -> Component.literal("Started...").withStyle(ChatFormatting.YELLOW);
            case AVAILABLE -> Component.literal("Can start...").withStyle(ChatFormatting.YELLOW);
            case UNAVAILABLE -> Component.literal("Cannot start...").withStyle(ChatFormatting.RED);
            case COMPLETED -> Component.literal("Completed!").withStyle(ChatFormatting.GREEN);
        };
    }
}
