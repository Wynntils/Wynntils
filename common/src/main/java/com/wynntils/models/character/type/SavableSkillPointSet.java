/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record SavableSkillPointSet(
        int strength,
        int dexterity,
        int intelligence,
        int defence,
        int agility,
        String weapon,
        List<String> armourNames,
        List<String> accessoryNames) {
    /**
     * Constructs a new SavableSkillPointSet representing just a loadout.
     */
    public SavableSkillPointSet(int[] skillPoints) {
        this(skillPoints, null, new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Constructs a new SavableSkillPointSet representing a full build with gear.
     */
    public SavableSkillPointSet(
            int[] skillPoints, String weapon, List<String> armourNames, List<String> accessoryNames) {
        this(
                skillPoints[0],
                skillPoints[1],
                skillPoints[2],
                skillPoints[3],
                skillPoints[4],
                weapon,
                Collections.unmodifiableList(armourNames),
                Collections.unmodifiableList(accessoryNames));
    }

    public int[] getSkillPointsAsArray() {
        return new int[] {strength, dexterity, intelligence, defence, agility};
    }

    public int getSkillPointsSum() {
        return strength + dexterity + intelligence + defence + agility;
    }

    public int getMinimumCombatLevel() {
        return (int) Math.ceil(getSkillPointsSum() / 2.0) + 1;
    }

    public boolean isBuild() {
        return weapon != null || !armourNames.isEmpty() || !accessoryNames.isEmpty();
    }
}
