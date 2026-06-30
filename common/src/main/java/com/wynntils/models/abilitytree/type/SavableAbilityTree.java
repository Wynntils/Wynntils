package com.wynntils.models.abilitytree.type;

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

    public int getDisplayLevel() {
        if (info == null || info.nodes().isEmpty()) return 1;
        return info.nodes().stream()
                .mapToInt(AbilityTreeSkillNode::requiredLevel)
                .max()
                .orElse(1);
    }

    public int getNodeCount() {
        return info == null ? 0 : info.nodes().size();
    }
}
