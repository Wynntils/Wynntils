/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.discoveries.type;

import com.wynntils.models.activities.type.ActivityType;
import net.minecraft.ChatFormatting;

public enum DiscoveryType {
    TERRITORY(ChatFormatting.WHITE),
    WORLD(ChatFormatting.YELLOW),
    SECRET(ChatFormatting.AQUA);

    private final ChatFormatting color;

    DiscoveryType(ChatFormatting color) {
        this.color = color;
    }

    public static DiscoveryType fromContentType(ActivityType activityType) {
        return switch (activityType) {
            case WORLD_DISCOVERY -> WORLD;
            case TERRITORIAL_DISCOVERY -> TERRITORY;
            case SECRET_DISCOVERY -> SECRET;
            default -> throw new IllegalStateException("Unexpected value: " + activityType);
        };
    }

    public ChatFormatting getColor() {
        return color;
    }
}
