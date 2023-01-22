/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.spells.type;

import com.wynntils.core.components.Managers;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.gear.profile.IdentificationProfile;
import java.util.Arrays;

public enum SpellType {
    ARROW_STORM(ClassType.Archer, 1, "Arrow Storm", 6, 0),
    ESCAPE(ClassType.Archer, 2, "Escape", 3, 0),
    BOMB(ClassType.Archer, 3, "Arrow Bomb", 8, 0),
    ARROW_SHIELD(ClassType.Archer, 4, "Arrow Shield", 8, 1),

    SPIN_ATTACK(ClassType.Assassin, 1, "Spin Attack", 6, 0),
    DASH(ClassType.Assassin, 2, "Dash", 2, 0),
    MULTI_HIT(ClassType.Assassin, 3, "Multi Hit", 8, 0),
    SMOKE_BOMB(ClassType.Assassin, 4, "Smoke Bomb", 8, 0),

    BASH(ClassType.Warrior, 1, "Bash", 6, 0),
    CHARGE(ClassType.Warrior, 2, "Charge", 4, 0),
    UPPERCUT(ClassType.Warrior, 3, "Uppercut", 9, 0),
    WAR_SCREAM(ClassType.Warrior, 4, "War Scream", 7, -1),

    HEAL(ClassType.Mage, 1, "Heal", 8, -1),
    TELEPORT(ClassType.Mage, 2, "Teleport", 4, 0),
    METEOR(ClassType.Mage, 3, "Meteor", 8, 0),
    ICE_SNAKE(ClassType.Mage, 4, "Ice Snake", 6, -1),

    TOTEM(ClassType.Shaman, 1, "Totem", 4, 0),
    HAUL(ClassType.Shaman, 2, "Haul", 3, -1),
    AURA(ClassType.Shaman, 3, "Aura", 8, 0),
    UPROOT(ClassType.Shaman, 4, "Uproot", 6, 0),

    // Unspecified spells
    FIRST_SPELL(ClassType.None, 1, "1st Spell", 0, 0),
    SECOND_SPELL(ClassType.None, 2, "2nd Spell", 0, 0),
    THIRD_SPELL(ClassType.None, 3, "3rd Spell", 0, 0),
    FOURTH_SPELL(ClassType.None, 4, "4th Spell", 0, 0);

    private static final SpellDirection[] RLR = {SpellDirection.RIGHT, SpellDirection.LEFT, SpellDirection.RIGHT};
    private static final SpellDirection[] RRR = {SpellDirection.RIGHT, SpellDirection.RIGHT, SpellDirection.RIGHT};
    private static final SpellDirection[] RLL = {SpellDirection.RIGHT, SpellDirection.LEFT, SpellDirection.LEFT};
    private static final SpellDirection[] RRL = {SpellDirection.RIGHT, SpellDirection.RIGHT, SpellDirection.LEFT};
    // Archer only
    private static final SpellDirection[] LRL = SpellDirection.invertArray(RLR);
    private static final SpellDirection[] LLL = SpellDirection.invertArray(RRR);
    private static final SpellDirection[] LRR = SpellDirection.invertArray(RLL);
    private static final SpellDirection[] LLR = SpellDirection.invertArray(RRL);

    private static final int[][] MANA_REDUCTION_LEVELS = {
        {},
        {68},
        {41, 105},
        {29, 68, 129},
        {23, 51, 89, 147},
        {19, 41, 68, 105},
        {16, 34, 55, 82, 118},
        {14, 29, 47, 68, 94, 129},
        {12, 26, 41, 58, 79, 105, 139},
        {11, 23, 36, 51, 68, 89, 114, 147}
    };

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

    public int getUnlockLevel(int grade) {
        int unlockLevel = (spellNumber - 1) * 10 + 1;
        if (grade == 1) return unlockLevel;
        if (grade == 2) return unlockLevel + 15;
        if (grade == 3) return unlockLevel + 35;
        assert (false);
        return 0;
    }

    public int getGrade(int level) {
        int compareLevel = level - (spellNumber - 1) * 10;
        if (compareLevel >= 36) {
            return 3;
        } else if (compareLevel >= 16) {
            return 2;
        } else if (compareLevel >= 1) {
            return 1;
        } else {
            // not unlocked
            return 0;
        }
    }

    private int getUnreducedManaCost(int level) {
        return startManaCost + (getGrade(level) - 1) * gradeManaChange;
    }

    public int getManaCost(int level, int intelligenceLevel) {
        int manaReduction = 0;
        for (int i : MANA_REDUCTION_LEVELS[getUnreducedManaCost(level) - 1]) {
            if (intelligenceLevel >= i) {
                manaReduction++;
            } else {
                break;
            }
        }
        return getUnreducedManaCost(level) - manaReduction;
    }

    public int getNextManaReduction(int level, int intelligenceLevel) {
        for (int i : MANA_REDUCTION_LEVELS[getUnreducedManaCost(level) - 1]) {
            if (i > intelligenceLevel) {
                return i;
            }
        }
        return Integer.MAX_VALUE;
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
        return forClass(ClassType.None, getSpellNumber()).getName();
    }

    public String getGenericAndSpecificName() {
        return getGenericName() + " (" + getName() + ")";
    }

    public String getShortIdName(boolean isRaw) {
        return IdentificationProfile.getAsShortName(getGenericName() + " Cost", isRaw);
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
        return forClass(Managers.Character.getClassType(), spellNumber);
    }
}
