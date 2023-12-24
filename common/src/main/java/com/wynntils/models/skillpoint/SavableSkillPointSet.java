package com.wynntils.models.skillpoint;

public class SavableSkillPointSet {
    private final boolean isBuild;
    private final int strength;
    private final int dexterity;
    private final int intelligence;
    private final int defence;
    private final int agility;

    public SavableSkillPointSet(boolean isBuild, int strength, int dexterity, int intelligence, int defence, int agility) {
        this.isBuild = isBuild;
        this.strength = strength;
        this.dexterity = dexterity;
        this.intelligence = intelligence;
        this.defence = defence;
        this.agility = agility;
    }

    public int getStrength() {
        return strength;
    }

    public int getDexterity() {
        return dexterity;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public int getDefence() {
        return defence;
    }

    public int getAgility() {
        return agility;
    }

    public int[] getSkillPointsAsArray() {
        return new int[] {strength, dexterity, intelligence, defence, agility};
    }
}