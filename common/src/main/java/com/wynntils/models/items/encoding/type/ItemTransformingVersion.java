/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.type;

public enum ItemTransformingVersion {
    VERSION_1(0);

    private final byte id;

    ItemTransformingVersion(int id) {
        this.id = (byte) id;
    }

    public static ItemTransformingVersion fromId(byte id) {
        return switch (id) {
            case 0 -> VERSION_1;
            default -> null;
        };
    }

    public byte getId() {
        return id;
    }
}
