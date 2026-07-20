/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.type;

import com.wynntils.core.components.Models;
import com.wynntils.models.character.type.ClassType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record SavableAbilityTree(List<String> abilities, ClassType classType) {
    public String getMainArchetype() {
        if (abilities == null || abilities.isEmpty()) return null;
        Map<String, Integer> counts = new HashMap<>();
        for (String abilityName : abilities) {
            AbilityTreeSkillNode node = Models.AbilityTree.getNodeFromNameAndClass(abilityName, classType);
            if (node == null || node.archetypeInfo() == null) continue;
            String a = node.archetypeInfo().archetype();
            if (a != null && !a.isEmpty()) {
                counts.merge(a, 1, Integer::sum);
            }
        }
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public int getLevel() {
        if (abilities == null || abilities.isEmpty()) return 1;
        int abilityPoints = 0;
        for (String abilityName : abilities) {
            AbilityTreeSkillNode node = Models.AbilityTree.getNodeFromNameAndClass(abilityName, classType);
            if (node != null) {
                abilityPoints += node.cost();
            }
        }
        return AbilityPointProgression.getLevelForPoints(abilityPoints);
    }

    public int getNodeCount() {
        return abilities == null ? 0 : abilities.size();
    }
}
