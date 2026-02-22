/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map.type;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum TerritoryInfoType {
    RESOURCE("Resource Types"),
    DEFENSE("Defense Level"),
    PRODUCTION("Production Upgrades"),
    SEEKING("Seekings");

    private final String asString;

    TerritoryInfoType(String asString) {
        this.asString = asString;
    }

    public Component asComponent() {
        return Component.literal(asString).withStyle(ChatFormatting.WHITE);
    }

    public TerritoryInfoType getNext() {
        return values()[(ordinal() + 1) % values().length];
    }

    public TerritoryInfoType getPrevious() {
        return values()[(ordinal() - 1) % values().length];
    }
}
