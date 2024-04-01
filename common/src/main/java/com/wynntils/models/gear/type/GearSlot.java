/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gear.type;

public enum GearSlot {
    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS,
    RING1,
    RING2,
    BRACELET,
    NECKLACE,
    WEAPON;

    /**
     * Rings must be handled manually since there are two possible slots
     */
    public static GearSlot fromGearType(GearType gearType) {
        return switch (gearType) {
            case SPEAR, BOW, WAND, DAGGER, RELIK, WEAPON -> WEAPON;

            case HELMET -> HELMET;
            case CHESTPLATE -> CHESTPLATE;
            case LEGGINGS -> LEGGINGS;
            case BOOTS -> BOOTS;
            case BRACELET -> BRACELET;
            case NECKLACE -> NECKLACE;

            default -> throw new IllegalArgumentException("Unhandled gear type trying to cast to SetSlot: " + gearType);
        };
    }
}
