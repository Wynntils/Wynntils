/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.abilitytree.type;

import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

public enum AbilityTreeQueryState {
    // Actively doing a query
    PARSING(Component.translatable("screens.wynntils.abilityTree.parsing")),

    // Successful query
    PARSED(() -> null),

    // Errors occurred during parsing
    ERROR_CLASS_NOT_PARSED(Component.translatable("screens.wynntils.abilityTree.errorClassNotParsed")),
    ERROR_NO_CLASS_DATA(Component.translatable("screens.wynntils.abilityTree.errorNoClassData")),
    ERROR_PARSING_INSTANCE(Component.translatable("screens.wynntils.abilityTree.errorParsingInstance")),
    ERROR_API_INFO_OUTDATED(Component.translatable("screens.wynntils.abilityTree.apiInfoOutdated"));

    private final Supplier<Component> componentSupplier;

    AbilityTreeQueryState(Component component) {
        this.componentSupplier = () -> component;
    }

    AbilityTreeQueryState(Supplier<Component> componentSupplier) {
        this.componentSupplier = componentSupplier;
    }

    public Component getComponent() {
        return componentSupplier.get();
    }

    /**
     * @return true if the query is in a state where the screen can accept user input
     */
    public boolean isReady() {
        return this == PARSED;
    }
}
