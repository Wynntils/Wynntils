/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

public enum ConsumableType {
    POTION(0),
    FOOD(1),
    SCROLL(2);

    private final int encodingId;

    ConsumableType(int encodingId) {
        this.encodingId = encodingId;
    }

    public static ConsumableType fromString(String name) {
        for (ConsumableType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }

        return null;
    }

    public static ConsumableType fromEncodingId(int id) {
        for (ConsumableType value : values()) {
            if (value.encodingId == id) {
                return value;
            }
        }

        return null;
    }

    public int getEncodingId() {
        return encodingId;
    }
}
