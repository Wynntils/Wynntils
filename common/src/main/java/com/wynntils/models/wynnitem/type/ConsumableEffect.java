/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem.type;

public enum ConsumableEffect {
    HEAL("§c❤"),
    MANA("§b✺"),
    DURATION("Seconds");

    private final String suffix;

    ConsumableEffect(String suffix) {
        this.suffix = suffix;
    }

    public static ConsumableEffect fromString(String type) {
        for (ConsumableEffect value : values()) {
            if (value.name().equalsIgnoreCase(type)) {
                return value;
            }
        }

        return null;
    }

    public String getSuffix() {
        return suffix;
    }
}
