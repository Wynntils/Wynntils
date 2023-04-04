/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.discoveries.type;

import com.wynntils.core.text.StyledText;
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

    public static DiscoveryType getDiscoveryTypeFromString(StyledText name) {
        for (DiscoveryType type : values()) {
            if (name.str().charAt(1) == type.getColor().getChar()) {
                return type;
            }
        }
        return null;
    }

    public int getOrder() {
        return order;
    }

    public ChatFormatting getColor() {
        return color;
    }
}
