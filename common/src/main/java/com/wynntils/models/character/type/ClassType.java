/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.type;

import com.wynntils.models.gear.type.GearType;

public enum ClassType {
    MAGE("Mage", "Dark Wizard", GearType.WAND),
    ARCHER("Archer", "Hunter", GearType.BOW),
    WARRIOR("Warrior", "Knight", GearType.SPEAR),
    ASSASSIN("Assassin", "Ninja", GearType.DAGGER),
    SHAMAN("Shaman", "Skyseer", GearType.RELIK),

    // This represents the class selection menu, or the generic spell
    NONE("none", "none", null);

    private final String name;
    private final String reskinnedName;
    private final GearType gearType;

    ClassType(String name, String reskinnedName, GearType gearType) {
        this.name = name;
        this.reskinnedName = reskinnedName;
        this.gearType = gearType;
    }

    public static ClassType fromName(String className) {
        for (ClassType type : values()) {
            if (className.equalsIgnoreCase(type.name) || className.equalsIgnoreCase(type.reskinnedName)) {
                return type;
            }
        }
        return ClassType.NONE;
    }

    public static boolean isReskinned(String className) {
        for (ClassType type : values()) {
            if (className.equalsIgnoreCase(type.name)) return false;
            if (className.equalsIgnoreCase(type.reskinnedName)) return true;
        }
        return false;
    }

    public String getName() {
        return name;
    }

    private String getReskinnedName() {
        return reskinnedName;
    }

    public String getActualName(boolean isReskinned) {
        return isReskinned ? getReskinnedName() : getName();
    }

    public String getFullName() {
        return name + "/" + reskinnedName;
    }

    public GearType getGearType() {
        return gearType;
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
