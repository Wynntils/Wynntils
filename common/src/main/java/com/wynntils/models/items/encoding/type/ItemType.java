/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.type;

public enum ItemType {
    GEAR(0),
    TOME(1),
    CHARM(2);

    private final byte id;

    ItemType(int id) {
        this.id = (byte) id;
    }

    public static ItemType fromId(byte id) {
        for (ItemType itemType : values()) {
            if (itemType.id == id) {
                return itemType;
            }
        }

        return null;
    }

    public byte getId() {
        return id;
    }
}
