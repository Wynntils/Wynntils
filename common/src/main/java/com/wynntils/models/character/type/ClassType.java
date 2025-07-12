/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.type;

import com.wynntils.utils.type.Pair;

public enum ClassType {
    MAGE("Mage", "Dark Wizard", 1, Pair.of("\uE002", "\uE007")),
    ARCHER("Archer", "Hunter", 2, Pair.of("\uE000", "\uE005")),
    WARRIOR("Warrior", "Knight", 3, Pair.of("\uE004", "\uE009")),
    ASSASSIN("Assassin", "Ninja", 4, Pair.of("\uE001", "\uE006")),
    SHAMAN("Shaman", "Skyseer", 5, Pair.of("\uE003", "\uE008")),

    // This represents the class selection menu, or the generic spell
    NONE("none", "none", 0, Pair.of("", ""));

    private final String name;
    private final String reskinnedName;
    private final int encodingId;
    private final Pair<String, String> cardCharactersPair;

    ClassType(String name, String reskinnedName, int encodingId, Pair<String, String> cardCharactersPair) {
        this.name = name;
        this.reskinnedName = reskinnedName;
        this.encodingId = encodingId;
        this.cardCharactersPair = cardCharactersPair;
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

    public static ClassType fromCharacterSelectionCard(String character) {
        for (ClassType type : values()) {
            if (type.cardCharactersPair.a().equals(character)
                    || type.cardCharactersPair.b().equals(character)) {
                return type;
            }
        }
        return NONE;
    }

    public static boolean isReskinnedCharacterSelection(ClassType classType, String character) {
        // Whilst the assets are there, the character selection does not currently display the different textures for
        // reskins so this will always be false
        return classType.cardCharactersPair.b().equals(character);
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
