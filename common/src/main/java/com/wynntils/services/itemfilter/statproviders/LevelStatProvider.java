/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.LeveledItemProperty;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.List;
import java.util.Optional;

public class LevelStatProvider extends ItemStatProvider<Integer> {
    @Override
    public Optional<Integer> getValue(WynnItem wynnItem) {
        if (!(wynnItem instanceof LeveledItemProperty levelItemProperty)) return Optional.empty();

        return Optional.of(levelItemProperty.getLevel());
    }

    @Override
    public List<ItemProviderType> getFilterTypes() {
        return List.of(ItemProviderType.GENERIC);
    }

    @Override
    public List<String> getAliases() {
        return List.of("lvl");
    }
}
