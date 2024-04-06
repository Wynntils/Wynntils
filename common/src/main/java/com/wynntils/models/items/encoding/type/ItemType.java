/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.type;

public enum ItemType {
    GEAR(0),
    TOME(1),
    CHARM(2),
    CRAFTED_GEAR(3),
    CRAFTED_CONSUMABLE(4);

    private final byte encodingId;

    ItemType(int encodingId) {
        this.encodingId = (byte) encodingId;
    }

    public static ItemType fromEncodingId(byte id) {
        for (ItemType itemType : values()) {
            if (itemType.encodingId == id) {
                return itemType;
            }
        }

        return null;
    }

    public byte getEncodingId() {
        return encodingId;
    }
}
