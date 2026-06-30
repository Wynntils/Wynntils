package com.wynntils.screens.buildloadouts.type;

import com.wynntils.models.abilitytree.type.SavableAbilityTree;
import com.wynntils.models.character.type.SavableSkillPointSet;

public record Loadout(
        String name,
        SavableSkillPointSet skillPoints,   // null if no SP component
        SavableAbilityTree abilityTree,     // null if no AT component
        LoadoutType type
) {
    public boolean hasSkillPoints() { return skillPoints != null; }
    public boolean hasAbilityTree() { return abilityTree != null; }
}
