/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.buildloadouts.type;

import com.wynntils.models.abilitytree.type.SavableAbilityTree;
import com.wynntils.models.aspects.type.SavableAspectSet;
import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.character.type.SavableSkillPointSet;

public record Loadout(
        String name,
        SavableSkillPointSet skillPoints,
        SavableAbilityTree abilityTree,
        SavableAspectSet aspect,
        LoadoutType type) {
    public boolean hasSkillPoints() {
        return skillPoints != null;
    }

    public boolean hasAbilityTree() {
        return abilityTree != null;
    }

    public boolean hasAspects() {
        return aspect != null;
    }

    public ClassType getClassType() {
        if (hasAbilityTree()) {
            ClassType atClass = abilityTree().getClassType();
            if (atClass != null && atClass != ClassType.NONE) {
                return atClass;
            }
        }
        if (hasAspects()) {
            ClassType aspectClass = aspect().classType();
            if (aspectClass != null && aspectClass != ClassType.NONE) {
                return aspectClass;
            }
        }
        return ClassType.NONE;
    }

    public boolean hasClassType() {
        return getClassType() != ClassType.NONE;
    }

    public String getMainArchetype() {
        return hasAbilityTree() ? abilityTree().getMainArchetype() : null;
    }

    public int getNodeCount() {
        return hasAbilityTree() ? abilityTree().getNodeCount() : 0;
    }

    public int getAspectCount() {
        return hasAspects() ? aspect().getAspectCount() : 0;
    }

    public int getMaxLevel() {
        int spLevel = hasSkillPoints() ? skillPoints().getMinimumCombatLevel() : 0;
        int atLevel = hasAbilityTree() ? abilityTree().getLevel() : 0;
        int asLevel = hasAspects() ? aspect().getLevel() : 0;

        return Math.max(1, Math.max(spLevel, Math.max(atLevel, asLevel)));
    }
}
