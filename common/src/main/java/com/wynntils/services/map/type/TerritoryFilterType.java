/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map.type;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum TerritoryFilterType {
    DEFAULT(""),
    HIGHER(" and higher"),
    LOWER(" and lower");

    private final String asString;

    TerritoryFilterType(String asString) {
        this.asString = asString;
    }

    public Component asComponent() {
        return Component.literal(asString).withStyle(ChatFormatting.WHITE);
    }
}
