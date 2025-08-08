/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.type;

public enum ItemTransformingVersion {
    VERSION_1(0),
    VERSION_2(1); // Added support for shiny rerolls

    private final byte id;

    ItemTransformingVersion(int id) {
        this.id = (byte) id;
    }

    public static ItemTransformingVersion fromId(byte id) {
        return switch (id) {
            case 0 -> VERSION_1;
            case 1 -> VERSION_2;
            default -> null;
        };
    }

    public byte getId() {
        return id;
    }
}
