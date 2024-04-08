/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.type;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;

public record ArchetypeInfo(
        String name, String formattedName, List<String> description, ItemInformation itemInformation) {
    public List<Component> getTooltip() {
        // The tooltip consists of the name and description
        return Stream.concat(
                        Stream.of(Component.literal(formattedName)),
                        description.stream().map(Component::literal).map(c -> (Component) c))
                .toList();
    }
}
