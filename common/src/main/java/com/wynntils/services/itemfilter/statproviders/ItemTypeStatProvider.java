/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.core.components.Handlers;
import com.wynntils.handlers.item.GameItemAnnotator;
import com.wynntils.models.items.WynnItem;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemTypeStatProvider extends ItemStatProvider<String> {
    @Override
    public Optional<String> getValue(WynnItem wynnItem) {
        return Optional.of(wynnItem.getClass().getSimpleName().replace("Item", ""));
    }

    @Override
    public List<String> getValidInputs() {
        return Handlers.Item.getAnnotators().stream()
                .filter(annotator -> annotator instanceof GameItemAnnotator)
                .map(annotator -> annotator.getClass().getSimpleName().replace("Annotator", ""))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemProviderType> getFilterTypes() {
        return List.of(ItemProviderType.GENERIC);
    }
}
