/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.type;

public enum ClassType {
    MAGE("Mage", "Dark Wizard", 1, "\uE002", "\uE007"),
    ARCHER("Archer", "Hunter", 2, "\uE000", "\uE005"),
    WARRIOR("Warrior", "Knight", 3, "\uE004", "\uE009"),
    ASSASSIN("Assassin", "Ninja", 4, "\uE001", "\uE006"),
    SHAMAN("Shaman", "Skyseer", 5, "\uE003", "\uE008"),

    // This represents the class selection menu, or the generic spell
    NONE("none", "none", 0, "", "");

    private final String name;
    private final String reskinnedName;
    private final int encodingId;
    // These represent the characters used to display the side card on the character selection screen
    private final String cardCharacter;
    private final String cardCharacterReskinned;

    ClassType(String name, String reskinnedName, int encodingId, String cardCharacter, String cardCharacterReskinned) {
        this.name = name;
        this.reskinnedName = reskinnedName;
        this.encodingId = encodingId;
        this.cardCharacter = cardCharacter;
        this.cardCharacterReskinned = cardCharacterReskinned;
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
            if (type.cardCharacter.equals(character) || type.cardCharacterReskinned.equals(character)) {
                return type;
            }
        }
        return NONE;
    }

    public static boolean isReskinnedCharacterSelection(ClassType classType, String character) {
        // Whilst the assets are there, the character selection does not currently display the different textures for
        // reskins so this will always be false
        return classType.cardCharacterReskinned.equals(character);
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
