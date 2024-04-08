/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.type;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public record ArchetypeInfo(
        String name, String formattedName, List<String> description, ItemInformation itemInformation) {
    public List<Component> getTooltip(ParsedAbilityTree abilityTree) {
        // The tooltip consists of the name and description
        int archetypeCount = (int) abilityTree.nodes().keySet().stream()
                .filter(node -> node.archetype() != null)
                .filter(node -> node.archetype().equalsIgnoreCase(name))
                .count();

        int unlockedCount = (int) abilityTree.nodes().keySet().stream()
                .filter(node -> node.archetype() != null)
                .filter(node -> node.archetype().equalsIgnoreCase(name))
                .filter(node -> abilityTree.getNodeState(node) == AbilityTreeNodeState.UNLOCKED)
                .count();

        return Stream.concat(
                        Stream.concat(
                                Stream.of(Component.literal(formattedName)),
                                description.stream().map(Component::literal).map(c -> (Component) c)),
                        Stream.of(
                                Component.empty(),
                                Component.literal("✔ ")
                                        .withStyle(ChatFormatting.GREEN)
                                        .append(Component.translatable(
                                                        "screens.wynntils.abilityTree.archetype.unlockedAbilities")
                                                .withStyle(ChatFormatting.GRAY)
                                                .append(Component.literal(": "))
                                                .append(Component.literal(String.valueOf(unlockedCount))
                                                        .withStyle(ChatFormatting.WHITE))
                                                .append(Component.literal("/"))
                                                .append(Component.literal(String.valueOf(archetypeCount))
                                                        .withStyle(ChatFormatting.GRAY)))))
                .toList();
    }
}
