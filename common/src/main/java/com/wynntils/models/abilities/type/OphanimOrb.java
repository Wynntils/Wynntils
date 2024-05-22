/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilities.type;

public class OphanimOrb {
    private final HealthState healthState;

    public OphanimOrb(HealthState healthState) {
        this.healthState = healthState;
    }

    public String getString() {
        return healthState.getColorCode() + "⏺";
    }

    public enum HealthState {
        HEALTHY("§b"),
        DAMAGED("§c"),
        DYING("§e"),
        DEAD("§7");

        private final String colorCode;

        HealthState(String colorCode) {
            this.colorCode = colorCode;
        }

        public static HealthState fromColorCode(String colorCode) {
            for (HealthState value : HealthState.values()) {
                if (value.getColorCode().equals(colorCode)) {
                    return value;
                }
            }

            return null;
        }

        public String getColorCode() {
            return colorCode;
        }
    }
}
