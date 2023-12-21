/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.type;

public enum ItemType {
    GEAR(0);

    private final byte id;

    ItemType(int id) {
        this.id = (byte) id;
    }

    public static ItemType fromId(byte id) {
        return switch (id) {
            case 0 -> GEAR;
            default -> null;
        };
    }

    public byte getId() {
        return id;
    }
}
