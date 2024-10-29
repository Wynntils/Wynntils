/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.type;

public enum ClassType {
    MAGE("Mage", "Dark Wizard", 1),
    ARCHER("Archer", "Hunter", 2),
    WARRIOR("Warrior", "Knight", 3),
    ASSASSIN("Assassin", "Ninja", 4),
    SHAMAN("Shaman", "Skyseer", 5),

    // This represents the class selection menu, or the generic spell
    NONE("none", "none", 0);

    private final String name;
    private final String reskinnedName;
    private final int encodingId;

    ClassType(String name, String reskinnedName, int encodingId) {
        this.name = name;
        this.reskinnedName = reskinnedName;
        this.encodingId = encodingId;
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

    public int getEncodingId() {
        return encodingId;
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
