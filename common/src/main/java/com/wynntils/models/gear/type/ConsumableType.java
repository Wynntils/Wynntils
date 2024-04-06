/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

public enum ConsumableType {
    POTION(0),
    FOOD(1),
    SCROLL(2),
    // This is a fallback for when the type is unknown and can't be parsed.
    CONSUMABLE(3);

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

        return ConsumableType.CONSUMABLE;
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
