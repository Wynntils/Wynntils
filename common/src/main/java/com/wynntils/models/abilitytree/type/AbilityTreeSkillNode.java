/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.type;

import java.util.List;
import java.util.Objects;

public record AbilityTreeSkillNode(
        String name,
        String formattedName,
        List<String> description,
        ItemInformation itemInformation,
        int cost,
        List<String> blocks,
        String requiredAbility,
        ArchetypeRequirement requiredArchetype,
        String archetype,
        AbilityTreeLocation location,
        List<String> connections) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbilityTreeSkillNode that = (AbilityTreeSkillNode) o;
        return cost == that.cost
                && Objects.equals(formattedName, that.formattedName)
                && Objects.equals(requiredAbility, that.requiredAbility)
                && Objects.equals(requiredArchetype, that.requiredArchetype)
                && Objects.equals(archetype, that.archetype)
                && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formattedName, cost, requiredAbility, requiredArchetype, archetype, location);
    }
}
