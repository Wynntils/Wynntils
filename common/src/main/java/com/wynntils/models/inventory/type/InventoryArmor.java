/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.inventory.type;

import java.util.Locale;

public enum InventoryArmor {
    HELMET("Helmet", 3),
    CHESTPLATE("Chestplate", 2),
    LEGGINGS("Leggings", 1),
    BOOTS("Boots", 0);

    private final String armorName;
    private final int armorSlot;

    InventoryArmor(String armorName, int armorSlot) {
        this.armorName = armorName;
        this.armorSlot = armorSlot;
    }

    public int getSlot() {
        return armorSlot;
    }

    public static InventoryArmor fromString(String type) {
        try {
            return valueOf(type.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static int[] getSlots() {
        int[] slots = new int[values().length];

        for (int i = 0; i < values().length; i++) {
            slots[i] = values()[i].getSlot();
        }

        return slots;
    }
}
