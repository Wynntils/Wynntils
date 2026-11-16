/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.profession.type;

import java.util.Locale;

public enum CraftedItemType {
    SPEAR,
    WAND,
    DAGGER,
    BOW,
    RELIK,
    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS,
    RING,
    BRACELET,
    NECKLACE,
    POTION,
    FOOD,
    SCROLL;

    public static CraftedItemType fromString(String typeStr) {
        try {
            return CraftedItemType.valueOf(typeStr.toUpperCase(Locale.ROOT).replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isGear() {
        return this != POTION && this != FOOD && this != SCROLL;
    }

    public boolean isAccessory() {
        return this == RING || this == BRACELET || this == NECKLACE;
    }
}
