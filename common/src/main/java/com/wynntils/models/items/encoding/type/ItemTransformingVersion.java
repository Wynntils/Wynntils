/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.type;

public enum ItemTransformingVersion {
    VERSION_1(0),
    VERSION_2(1), // Added support for shiny rerolls
    VERSION_3(2); // Updated internal roll handling and removing crafted effect strength

    private final byte id;

    ItemTransformingVersion(int id) {
        this.id = (byte) id;
    }

    public static ItemTransformingVersion fromId(byte id) {
        return switch (id) {
            case 0 -> VERSION_1;
            case 1 -> VERSION_2;
            case 2 -> VERSION_3;
            default -> null;
        };
    }

    public byte getId() {
        return id;
    }
}
