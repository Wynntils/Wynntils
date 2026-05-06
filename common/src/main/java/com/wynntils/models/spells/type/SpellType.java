/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells.type;

import com.wynntils.core.components.Models;
import com.wynntils.models.character.type.ClassType;
import java.util.Arrays;
import java.util.List;

public enum SpellType {
    ARROW_STORM(ClassType.ARCHER, 1, "Arrow Storm", 6, 0),
    ESCAPE(ClassType.ARCHER, 2, "Escape", 3, 0),
    BOMB(ClassType.ARCHER, 3, "Arrow Bomb", 8, 0),
    ARROW_SHIELD(ClassType.ARCHER, 4, "Arrow Shield", 8, 1),

    SPIN_ATTACK(ClassType.ASSASSIN, 1, "Spin Attack", 6, 0),
    DASH(ClassType.ASSASSIN, 2, "Dash", 2, 0),
    MULTIHIT(ClassType.ASSASSIN, 3, "Multihit", 8, 0),
    SMOKE_BOMB(ClassType.ASSASSIN, 4, "Smoke Bomb", 8, 0),

    BASH(ClassType.WARRIOR, 1, "Bash", 6, 0),
    CHARGE(ClassType.WARRIOR, 2, "Charge", 4, 0),
    UPPERCUT(ClassType.WARRIOR, 3, "Uppercut", 9, 0),
    WAR_SCREAM(ClassType.WARRIOR, 4, "War Scream", 7, -1),

    HEAL(ClassType.MAGE, 1, "Heal", 8, -1),
    TELEPORT(ClassType.MAGE, 2, "Teleport", 4, 0),
    METEOR(ClassType.MAGE, 3, "Meteor", 8, 0),
    ICE_SNAKE(ClassType.MAGE, 4, "Ice Snake", 6, -1),

    TOTEM(ClassType.SHAMAN, 1, "Totem", 4, 0),
    HAUL(ClassType.SHAMAN, 2, "Haul", 3, -1),
    AURA(ClassType.SHAMAN, 3, "Aura", 8, 0),
    UPROOT(ClassType.SHAMAN, 4, "Uproot", 6, 0),

    // Unspecified spells
    FIRST_SPELL(ClassType.NONE, 1, "1st Spell", 0, 0),
    SECOND_SPELL(ClassType.NONE, 2, "2nd Spell", 0, 0),
    THIRD_SPELL(ClassType.NONE, 3, "3rd Spell", 0, 0),
    FOURTH_SPELL(ClassType.NONE, 4, "4th Spell", 0, 0);

    public static final int MAX_SPELL = 4;

    private static final List<SpellDirection[]> SPELL_COMBOS = List.of(
            new SpellDirection[] {SpellDirection.RIGHT, SpellDirection.LEFT, SpellDirection.RIGHT},
            new SpellDirection[] {SpellDirection.RIGHT, SpellDirection.RIGHT, SpellDirection.RIGHT},
            new SpellDirection[] {SpellDirection.RIGHT, SpellDirection.LEFT, SpellDirection.LEFT},
            new SpellDirection[] {SpellDirection.RIGHT, SpellDirection.RIGHT, SpellDirection.LEFT});

    private final ClassType classType;
    private final int spellNumber;
    private final String name;
    private final int startManaCost;
    private final int gradeManaChange;

    SpellType(ClassType classType, int spellNumber, String name, int startManaCost, int gradeManaChange) {
        this.classType = classType;
        this.spellNumber = spellNumber;
        this.name = name;
        this.startManaCost = startManaCost;
        this.gradeManaChange = gradeManaChange;
    }

    public ClassType getClassType() {
        return classType;
    }

    public int getSpellNumber() {
        return spellNumber;
    }

    public String getName() {
        return name;
    }

    public String getGenericName() {
        return forClass(ClassType.NONE, getSpellNumber()).getName();
    }

    public String getGenericAndSpecificName() {
        return getGenericName() + " (" + getName() + ")";
    }

    public SpellType forOtherClass(ClassType otherClass) {
        return forClass(otherClass, getSpellNumber());
    }

    public SpellDirection[] getSpellDirectionArray() {
        SpellDirection[] directionArray = SPELL_COMBOS.get(spellNumber - 1);
        return Arrays.copyOf(directionArray, directionArray.length);
    }

    public static SpellType fromName(String name) {
        for (SpellType spellType : values()) {
            // After the matching part, the string needs to be done, or a blank character
            // must appear
            if (name.startsWith(spellType.name)
                    && (name.length() == spellType.name.length()
                            || String.valueOf(name.charAt(spellType.name.length()))
                                    .isBlank())) {
                return spellType;
            }
        }
        return null;
    }

    public static SpellType forClass(ClassType classRequired, int spellNumber) {
        for (SpellType spellType : values()) {
            if (spellType.classType == classRequired && spellType.spellNumber == spellNumber) {
                return spellType;
            }
        }
        return null;
    }

    public static int getSpellNumberFromDirectionArray(SpellDirection[] spellDirections) {
        SpellDirection[] normalizedDirections = spellDirections[0] == SpellDirection.LEFT
                ? SpellDirection.invertArray(spellDirections)
                : spellDirections;

        for (int spellNumber = 1; spellNumber <= SpellType.MAX_SPELL; spellNumber++) {
            if (Arrays.equals(normalizedDirections, SPELL_COMBOS.get(spellNumber - 1))) {
                return spellNumber;
            }
        }
        return -1;
    }

    public static SpellType fromSpellDirectionArray(ClassType classType, SpellDirection[] casted) {
        int spellNumber = getSpellNumberFromDirectionArray(casted);
        if (spellNumber == -1) return null;

        return forClass(classType, spellNumber);
    }

    public static SpellType fromSpellDirectionArray(SpellDirection[] casted) {
        return fromSpellDirectionArray(Models.Character.getClassType(), casted);
    }

    public static SpellDirection[] getSpellDirectionArrayFromString(String casted) {
        // Convert e.g. "rlr" into a spell direction array
        SpellDirection[] spellDirections = new SpellDirection[casted.length()];
        for (int i = 0; i < spellDirections.length; i++) {
            char directionChar = casted.charAt(i);
            if (directionChar == 'r' || directionChar == 'R') {
                spellDirections[i] = SpellDirection.RIGHT;
            } else if (directionChar == 'l' || directionChar == 'L') {
                spellDirections[i] = SpellDirection.LEFT;
            } else {
                throw new IllegalArgumentException("Invalid spell direction character: " + directionChar);
            }
        }
        return spellDirections;
    }

    public static SpellType fromSpellString(ClassType classType, String casted) {
        return fromSpellDirectionArray(classType, getSpellDirectionArrayFromString(casted));
    }
}
