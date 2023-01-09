/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects;

import com.google.gson.annotations.SerializedName;

public enum ClassType {
    @SerializedName("MAGE")
    Mage("Mage", "Dark Wizard"),
    @SerializedName("ARCHER")
    Archer("Archer", "Hunter"),
    @SerializedName("WARRIOR")
    Warrior("Warrior", "Knight"),
    @SerializedName("ASSASSIN")
    Assassin("Assassin", "Ninja"),
    @SerializedName("SHAMAN")
    Shaman("Shaman", "Skyseer"),

    // This represents the class selection menu, or the generic spell
    None("none", "none");

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
        return ClassType.None;
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

    @Override
    public String toString() {
        return name + "/" + reskinnedName;
    }
}
