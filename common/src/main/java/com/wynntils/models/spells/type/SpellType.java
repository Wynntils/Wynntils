/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells.type;

import com.wynntils.core.components.Models;
import com.wynntils.models.character.type.ClassType;
import java.util.Arrays;

public enum SpellType {
    ARROW_STORM(ClassType.ARCHER, 1, "Arrow Storm", 6, 0),
    ESCAPE(ClassType.ARCHER, 2, "Escape", 3, 0),
    BOMB(ClassType.ARCHER, 3, "Arrow Bomb", 8, 0),
    ARROW_SHIELD(ClassType.ARCHER, 4, "Arrow Shield", 8, 1),

    SPIN_ATTACK(ClassType.ASSASSIN, 1, "Spin Attack", 6, 0),
    DASH(ClassType.ASSASSIN, 2, "Dash", 2, 0),
    MULTI_HIT(ClassType.ASSASSIN, 3, "Multi Hit", 8, 0),
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

    private static final SpellDirection[] RLR = {SpellDirection.RIGHT, SpellDirection.LEFT, SpellDirection.RIGHT};
    private static final SpellDirection[] RRR = {SpellDirection.RIGHT, SpellDirection.RIGHT, SpellDirection.RIGHT};
    private static final SpellDirection[] RLL = {SpellDirection.RIGHT, SpellDirection.LEFT, SpellDirection.LEFT};
    private static final SpellDirection[] RRL = {SpellDirection.RIGHT, SpellDirection.RIGHT, SpellDirection.LEFT};
    // Archer only
    private static final SpellDirection[] LRL = SpellDirection.invertArray(RLR);
    private static final SpellDirection[] LLL = SpellDirection.invertArray(RRR);
    private static final SpellDirection[] LRR = SpellDirection.invertArray(RLL);
    private static final SpellDirection[] LLR = SpellDirection.invertArray(RRL);

    private final ClassType classType;
    private final int spellNumber;
    private final String name;
    private final int startManaCost;
    private final int gradeManaChange;

    public ClassType getClassType() {
        return classType;
    }

    public int getSpellNumber() {
        return spellNumber;
    }

    public String getName() {
        return name;
    }

    SpellType(ClassType classType, int spellNumber, String name, int startManaCost, int gradeManaChange) {
        this.classType = classType;
        this.spellNumber = spellNumber;
        this.name = name;
        this.startManaCost = startManaCost;
        this.gradeManaChange = gradeManaChange;
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

    public SpellType forOtherClass(ClassType otherClass) {
        return forClass(otherClass, getSpellNumber());
    }

    public static SpellType forClass(ClassType classRequired, int spellNumber) {
        for (SpellType spellType : values()) {
            if (spellType.classType == classRequired && spellType.spellNumber == spellNumber) {
                return spellType;
            }
        }
        return null;
    }

    public String getGenericName() {
        return forClass(ClassType.NONE, getSpellNumber()).getName();
    }

    public String getGenericAndSpecificName() {
        return getGenericName() + " (" + getName() + ")";
    }

    public static SpellType fromSpellDirectionArray(SpellDirection[] casted) {
        int spellNumber = 4;
        if (Arrays.equals(casted, RLR) || Arrays.equals(casted, LRL)) {
            spellNumber = 1;
        } else if (Arrays.equals(casted, RRR) || Arrays.equals(casted, LLL)) {
            spellNumber = 2;
        } else if (Arrays.equals(casted, RLL) || Arrays.equals(casted, LRR)) {
            spellNumber = 3;
        }
        return forClass(Models.Character.getClassType(), spellNumber);
    }
}
