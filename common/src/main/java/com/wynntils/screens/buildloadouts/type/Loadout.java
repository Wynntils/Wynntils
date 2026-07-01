package com.wynntils.screens.buildloadouts.type;

import com.wynntils.models.abilitytree.type.SavableAbilityTree;
import com.wynntils.models.aspects.type.SavableAspectSet;
import com.wynntils.models.character.type.SavableSkillPointSet;

public record Loadout(
        String name,
        SavableSkillPointSet skillPoints,
        SavableAbilityTree abilityTree,
        SavableAspectSet aspectLoadout,
        LoadoutType type
) {
    public boolean hasSkillPoints() { return skillPoints != null; }
    public boolean hasAbilityTree() { return abilityTree != null; }
    public boolean hasAspects() { return aspectLoadout != null; }

    public int getMaxLevel() {
        int spLevel = hasSkillPoints() ? skillPoints().getMinimumCombatLevel() : 0;
        int atLevel = hasAbilityTree() ? abilityTree().getDisplayLevel() : 0;
        int level = Math.max(spLevel, atLevel);
        if (level == 0) level = 1;

        return  level;
    }
}
