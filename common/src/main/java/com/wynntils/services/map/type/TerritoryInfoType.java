/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map.type;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum TerritoryInfoType {
    DEFENSE("Defense Level", ChatFormatting.RED),
    PRODUCTION("Production Upgrades", ChatFormatting.YELLOW),
    TREASURY("Treasury Bonus", ChatFormatting.LIGHT_PURPLE),
    SEEKING("Seekings", ChatFormatting.AQUA);

    private final String asString;
    private final ChatFormatting color;

    TerritoryInfoType(String asString, ChatFormatting color) {
        this.asString = asString;
        this.color = color;
    }

    public Component asComponent() {
        return Component.literal(asString).withStyle(color);
    }

    public TerritoryInfoType getNext() {
        return values()[(ordinal() + 1) % values().length];
    }

    public TerritoryInfoType getPrevious() {
        return values()[ordinal() == 0 ? values().length - 1 : ordinal() - 1];
    }
}
