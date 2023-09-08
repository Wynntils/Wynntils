/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.type;

public enum ClassType {
    MAGE("Mage", "Dark Wizard"),
    ARCHER("Archer", "Hunter"),
    WARRIOR("Warrior", "Knight"),
    ASSASSIN("Assassin", "Ninja"),
    SHAMAN("Shaman", "Skyseer"),

    // This represents the class selection menu, or the generic spell
    NONE("none", "none");

    private final String name;
    private final String reskinnedName;

    ClassType(String name, String reskinnedName) {
        this.name = name;
        this.reskinnedName = reskinnedName;
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

    @Override
    public String toString() {
        return getFullName();
    }
}
