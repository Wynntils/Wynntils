/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.gambits.type;

public enum Gambit {
    ANEMIC("Anemic's Gambit"),
    ARCANE_INCONTINENT("Arcane Incontinent's Gambit"),
    BLEEDING_WARRIOR("Bleeding Warrior's Gambit"),
    BURDENED_PACIFIST("Burdened Pacifist's Gambit"),
    CURSED_ALCHEMIST("Cursed Alchemist's Gambit"),
    DULL_BLADE("Dull Blade's Gambit"),
    ERODED_SPEEDSTER("Eroded Speedster's Gambit"),
    FARSIGHTED("Farsighted's Gambit"),
    FORESEEN_SWORDSMAN("Foreseen Swordsman's Gambit"),
    GLUTTON("Glutton's Gambit"),
    HEMOPHILIAC("Hemophiliac's Gambit"),
    INGENUOUS_MAGE("Ingenuous Mage's Gambit"),
    LEADEN_FIGHTER("Leaden Fighter's Gambit"),
    MADDENING_MAGE("Maddening Mage's Gambit"),
    MYOPIC("Myopic's Gambit"),
    OUTWORN_SOLDIER("Outworn Soldier's Gambit"),
    SHATTERED_MORTAL("Shattered Mortal's Gambit"),
    UNKNOWN("Unknown");

    private final String name;

    Gambit(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Gambit fromItemName(String itemName) {
        for (Gambit g : values()) {
            if (itemName.contains(g.getName())) {
                return g;
            }
        }
        return UNKNOWN;
    }
}
