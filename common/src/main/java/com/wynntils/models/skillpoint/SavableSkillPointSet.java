/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.skillpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record SavableSkillPointSet(
        int strength,
        int dexterity,
        int intelligence,
        int defence,
        int agility,
        List<String> armourNames,
        List<String> accessoryNames,
        boolean isBuild) {
    /**
     * Constructs a new SavableSkillPointSet representing just a loadout.
     */
    public SavableSkillPointSet(int[] skillPoints) {
        this(skillPoints, new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Constructs a new SavableSkillPointSet representing a full build with gear.
     */
    public SavableSkillPointSet(int[] skillPoints, List<String> armourNames, List<String> accessoryNames) {
        this(
                skillPoints[0],
                skillPoints[1],
                skillPoints[2],
                skillPoints[3],
                skillPoints[4],
                Collections.unmodifiableList(armourNames),
                Collections.unmodifiableList(accessoryNames),
                !armourNames.isEmpty() || !accessoryNames.isEmpty());
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
        return isBuild;
    }

    public List<String> getArmourNames() {
        return Collections.unmodifiableList(armourNames);
    }

    public List<String> getAccessoryNames() {
        return Collections.unmodifiableList(accessoryNames);
    }

    @Override
    public String toString() {
        return "SavableSkillPointSet{" + "isBuild="
                + isBuild + ", strength="
                + strength + ", dexterity="
                + dexterity + ", intelligence="
                + intelligence + ", defence="
                + defence + ", agility="
                + agility + ", armourNames="
                + armourNames + ", accessoryNames="
                + accessoryNames + '}';
    }
}
