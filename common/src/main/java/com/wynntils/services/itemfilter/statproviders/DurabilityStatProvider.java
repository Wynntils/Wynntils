/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.DurableItemProperty;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.Optional;

public class DurabilityStatProvider extends ItemStatProvider<CappedValue> {
    @Override
    public Optional<CappedValue> getValue(WynnItem wynnItem) {
        if (!(wynnItem instanceof DurableItemProperty durableItemProperty)) return Optional.empty();

        return Optional.of(durableItemProperty.getDurability());
    }

    @Override
    public List<ItemProviderType> getFilterTypes() {
        return List.of(ItemProviderType.DURABLE);
    }
}
