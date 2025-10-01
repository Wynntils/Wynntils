/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.inventory.type;

import java.util.Locale;

public enum InventoryArmor {
    HELMET("Helmet", 3, 39),
    CHESTPLATE("Chestplate", 2, 38),
    LEGGINGS("Leggings", 1, 37),
    BOOTS("Boots", 0, 36);

    private final String armorName;
    private final int armorSlot;
    private final int inventorySlot;

    InventoryArmor(String armorName, int armorSlot, int inventorySlot) {
        this.armorName = armorName;
        this.armorSlot = armorSlot;
        this.inventorySlot = inventorySlot;
    }

    public int getArmorSlot() {
        return armorSlot;
    }

    public int getInventorySlot() {
        return inventorySlot;
    }

    public static InventoryArmor fromString(String type) {
        try {
            return valueOf(type.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static int[] getArmorSlots() {
        int[] slots = new int[values().length];

        for (int i = 0; i < values().length; i++) {
            slots[i] = values()[i].getArmorSlot();
        }

        return slots;
    }
}
