/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.discoveries.type;

import com.wynntils.models.content.type.ContentType;
import net.minecraft.ChatFormatting;

public enum DiscoveryType {
    TERRITORY(0, ChatFormatting.WHITE),
    WORLD(1, ChatFormatting.YELLOW),
    SECRET(2, ChatFormatting.AQUA);

    private final int order;
    private final ChatFormatting color;

    DiscoveryType(int order, ChatFormatting color) {
        this.order = order;
        this.color = color;
    }

    public static DiscoveryType fromContentType(ContentType contentType) {
        return switch (contentType) {
            case WORLD_DISCOVERY -> WORLD;
            case TERRITORIAL_DISCOVERY -> TERRITORY;
            case SECRET_DISCOVERY -> SECRET;
            default -> throw new IllegalStateException("Unexpected value: " + contentType);
        };
    }

    public int getOrder() {
        return order;
    }

    public ChatFormatting getColor() {
        return color;
    }
}
