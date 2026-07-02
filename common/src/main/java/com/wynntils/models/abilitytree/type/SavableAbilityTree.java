/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.type;

import com.wynntils.models.character.type.ClassType;
import java.util.HashMap;
import java.util.Map;

public record SavableAbilityTree(AbilityTreeInfo info) {
    public String getMainArchetype() {
        if (info == null || info.nodes() == null || info.nodes().isEmpty()) return null;
        Map<String, Integer> counts = new HashMap<>();
        for (AbilityTreeSkillNode node : info.nodes()) {
            String a = node.archetype();
            if (a != null && !a.isEmpty()) counts.merge(a, 1, Integer::sum);
        }
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public int getLevel() {
        if (info == null || info.nodes().isEmpty()) return 1;
        int abilityPoints = AbilityPointProgression.getTotalPointsForTree(info);
        return AbilityPointProgression.getLevelForPoints(abilityPoints);
    }

    public int getNodeCount() {
        return info == null ? 0 : info.nodes().size();
    }

    public ClassType getClassType() {
        if (info == null || info.nodes() == null) return ClassType.NONE;
        return info.nodes().stream()
                .map(node -> node.abilityTreeNodeType().getClassType())
                .filter(classType -> classType != null && classType != ClassType.NONE)
                .findFirst()
                .orElse(ClassType.NONE);
    }
}
