/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

public enum ConsumableType {
    POTION(0),
    FOOD(1),
    SCROLL(2);

    private final int id;

    ConsumableType(int id) {
        this.id = id;
    }

    public static ConsumableType fromString(String name) {
        for (ConsumableType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }

        return null;
    }

    public static ConsumableType fromId(int id) {
        for (ConsumableType value : values()) {
            if (value.id == id) {
                return value;
            }
        }

        return null;
    }

    public int getId() {
        return id;
    }
}
