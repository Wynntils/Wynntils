/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

import net.minecraft.ChatFormatting;

public class OphanimOrb {
    private final HealthState healthState;

    public OphanimOrb(HealthState healthState) {
        this.healthState = healthState;
    }

    public String getString() {
        return healthState.getColor().toString() + "⏺";
    }

    public enum HealthState {
        HEALTHY(ChatFormatting.AQUA),
        DAMAGED(ChatFormatting.YELLOW),
        DYING(ChatFormatting.RED),
        DEAD(ChatFormatting.GRAY);

        private final ChatFormatting color;

        HealthState(ChatFormatting color) {
            this.color = color;
        }

        public static HealthState fromColor(ChatFormatting color) {
            for (HealthState value : HealthState.values()) {
                if (value.getColor() == color) {
                    return value;
                }
            }

            return null;
        }

        public ChatFormatting getColor() {
            return color;
        }
    }
}
