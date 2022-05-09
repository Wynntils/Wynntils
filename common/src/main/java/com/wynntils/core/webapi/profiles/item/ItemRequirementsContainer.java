/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi.profiles.item;

import com.wynntils.wc.objects.ClassType;

public class ItemRequirementsContainer {

    String quest = null;
    ClassType classType = null;
    int level = 0;

    int strength = 0;
    int dexterity = 0;
    int intelligence = 0;
    int defense = 0;
    int agility = 0;

    public ItemRequirementsContainer() {}

    public ClassType getClassType() {
        return classType;
    }

    public int getAgility() {
        return agility;
    }

    public int getDefense() {
        return defense;
    }

    public int getDexterity() {
        return dexterity;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public int getLevel() {
        return level;
    }

    public int getStrength() {
        return strength;
    }

    public String getQuest() {
        return quest;
    }

    public boolean requiresClass(ItemType type) {
        return getRealClass(type) != null;
    }

    public boolean requiresQuest() {
        return quest != null;
    }

    public boolean hasRequirements(ItemType type) {
        return requiresQuest()
                || requiresClass(type)
                || level != 0
                || strength != 0
                || dexterity != 0
                || intelligence != 0
                || defense != 0
                || agility != 0;
    }

    public ClassType getRealClass(ItemType type) {
        if (classType != null) return classType;

        if (type == ItemType.Wand) return ClassType.Mage;
        if (type == ItemType.Bow) return ClassType.Archer;
        if (type == ItemType.Spear) return ClassType.Warrior;
        if (type == ItemType.Dagger) return ClassType.Assassin;
        if (type == ItemType.Relik) return ClassType.Shaman;

        return null;
    }
}
