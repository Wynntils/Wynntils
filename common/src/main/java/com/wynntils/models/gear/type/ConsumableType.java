/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

public enum ConsumableType {
    POTION,
    FOOD,
    SCROLL;

    public static ConsumableType fromString(String name) {
        for (ConsumableType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }

        return null;
    }
}
