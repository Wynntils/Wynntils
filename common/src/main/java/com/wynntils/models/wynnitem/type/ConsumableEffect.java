/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnitem.type;

public enum ConsumableEffect {
    HEAL,
    MANA,
    DURATION;

    public static ConsumableEffect fromString(String type) {
        for (ConsumableEffect value : values()) {
            if (value.name().equalsIgnoreCase(type)) {
                return value;
            }
        }

        return null;
    }
}
