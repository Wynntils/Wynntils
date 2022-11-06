/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.discoveries.objects;

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

    public int getOrder() {
        return order;
    }

    public ChatFormatting getColor() {
        return color;
    }
}
