/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.type;

import java.util.List;
import java.util.Objects;

public record AbilityTreeSkillNode(
        int id,
        String name,
        String formattedName,
        AbilityTreeNodeType abilityTreeNodeType,
        List<String> description,
        int cost,
        List<String> willBlock,
        List<String> blockedBy,
        String requiredAbility,
        ArchetypeRequirement requiredArchetype,
        int requiredLevel,
        String archetype,
        AbilityTreeLocation location,
        List<Integer> connections) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbilityTreeSkillNode that = (AbilityTreeSkillNode) o;
        return id == that.id
                && cost == that.cost
                && Objects.equals(formattedName, that.formattedName)
                && Objects.equals(requiredAbility, that.requiredAbility)
                && Objects.equals(requiredArchetype, that.requiredArchetype)
                && Objects.equals(archetype, that.archetype)
                && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, formattedName, cost, requiredAbility, requiredArchetype, archetype, location);
    }

    public AbilityTreeSkillNode withDefaultType() {
        AbilityTreeNodeType defaultType = abilityTreeNodeType.getDefaultType();
        if (defaultType == abilityTreeNodeType) return this;
        return new AbilityTreeSkillNode(
                id,
                name,
                formattedName,
                defaultType,
                description,
                cost,
                willBlock,
                blockedBy,
                requiredAbility,
                requiredArchetype,
                requiredLevel,
                archetype,
                location,
                connections);
    }

    public AbilityTreeSkillNode withUnlockedType() {
        AbilityTreeNodeType unlockedType = abilityTreeNodeType.getUnlockedType();
        if (unlockedType == abilityTreeNodeType) return this;
        return new AbilityTreeSkillNode(
                id,
                name,
                formattedName,
                unlockedType,
                description,
                cost,
                willBlock,
                blockedBy,
                requiredAbility,
                requiredArchetype,
                requiredLevel,
                archetype,
                location,
                connections);
    }

    public AbilityTreeSkillNode withoutDescriptions() {
        return new AbilityTreeSkillNode(
                id,
                name,
                formattedName,
                abilityTreeNodeType,
                List.of(),
                cost,
                willBlock,
                blockedBy,
                requiredAbility,
                requiredArchetype,
                requiredLevel,
                archetype,
                location,
                connections);
    }
}
