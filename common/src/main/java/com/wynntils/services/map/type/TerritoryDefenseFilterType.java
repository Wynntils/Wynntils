/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map.type;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum TerritoryDefenseFilterType {
    DEFAULT(""),
    HIGHER(" and higher"),
    LOWER(" and lower");

    private final String asString;

    TerritoryDefenseFilterType(String asString) {
        this.asString = asString;
    }

    public Component asComponent() {
        return Component.literal(asString).withStyle(ChatFormatting.WHITE);
    }
}
