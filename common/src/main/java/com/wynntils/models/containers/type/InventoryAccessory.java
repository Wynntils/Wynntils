/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.containers.type;

import java.util.Locale;

public enum InventoryAccessory {
    RING_1("Ring_1", 9),
    RING_2("Ring_2", 10),
    BRACELET("Bracelet", 11),
    NECKLACE("Necklace", 12);

    private final String accessoryName;
    private final int inventorySlot;

    InventoryAccessory(String accessoryName, int inventorySlot) {
        this.accessoryName = accessoryName;
        this.inventorySlot = inventorySlot;
    }

    public int getSlot() {
        return inventorySlot;
    }

    public static InventoryAccessory fromString(String type) {
        try {
            return valueOf(type.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
