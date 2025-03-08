/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.ClassableItemProperty;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClassStatProvider extends ItemStatProvider<String> {
    @Override
    public Optional<String> getValue(WynnItem wynnItem) {
        if (!(wynnItem instanceof ClassableItemProperty classableItemProperty)) return Optional.empty();
        if (classableItemProperty.getRequiredClass() == null) return Optional.empty();

        return Optional.of(classableItemProperty.getRequiredClass().getName());
    }

    @Override
    public List<ItemProviderType> getFilterTypes() {
        return List.of(ItemProviderType.GEAR);
    }

    @Override
    public List<String> getValidInputs() {
        return Arrays.stream(ClassType.values()).map(ClassType::getName).collect(Collectors.toList());
    }
}
