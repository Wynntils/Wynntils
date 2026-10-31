/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.type;

public enum ItemTransformingVersion {
    VERSION_1(0),
    VERSION_2(1), // Added support for shiny rerolls
    VERSION_3(2), // Changed perfect internal roll and fix crafted support for Fruma-styled tooltips
    VERSION_4(3); // Numeric identification meters, actual powder tiers and powder specials
    // Be sure to update common/src/main/java/com/wynntils/models/items/ENCODING_STANDARD.md with changes when adding a
    // new version

    private final byte id;

    ItemTransformingVersion(int id) {
        this.id = (byte) id;
    }

    public static ItemTransformingVersion fromId(byte id) {
        return switch (id) {
            case 0 -> VERSION_1;
            case 1 -> VERSION_2;
            case 2 -> VERSION_3;
            case 3 -> VERSION_4;
            default -> null;
        };
    }

    public byte getId() {
        return id;
    }
}
