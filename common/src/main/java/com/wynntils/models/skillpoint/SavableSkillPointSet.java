/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.skillpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SavableSkillPointSet {
    private final boolean isBuild;
    private final int strength;
    private final int dexterity;
    private final int intelligence;
    private final int defence;
    private final int agility;
    private final List<String> armourNames;
    private final List<String> accessoryNames;

    public SavableSkillPointSet(int strength, int dexterity, int intelligence, int defence, int agility) {
        this.isBuild = false;
        this.strength = strength;
        this.dexterity = dexterity;
        this.intelligence = intelligence;
        this.defence = defence;
        this.agility = agility;
        this.armourNames = new ArrayList<>();
        this.accessoryNames = new ArrayList<>();
    }

    public SavableSkillPointSet(
            int strength,
            int dexterity,
            int intelligence,
            int defence,
            int agility,
            List<String> armourNames,
            List<String> accessoryNames) {
        this.isBuild = true;
        this.strength = strength;
        this.dexterity = dexterity;
        this.intelligence = intelligence;
        this.defence = defence;
        this.agility = agility;
        this.armourNames = new ArrayList<>(armourNames);
        this.accessoryNames = new ArrayList<>(accessoryNames);
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
        return !isBuild;
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
