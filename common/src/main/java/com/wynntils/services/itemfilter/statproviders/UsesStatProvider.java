/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.UsesItemProperty;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.Optional;

public class UsesStatProvider extends ItemStatProvider<CappedValue> {
    @Override
    public Optional<CappedValue> getValue(WynnItem wynnItem) {
        if (!(wynnItem instanceof UsesItemProperty usesItemProperty)) return Optional.empty();

        return Optional.of(usesItemProperty.getUses());
    }

    @Override
    public List<ItemProviderType> getFilterTypes() {
        return List.of(ItemProviderType.COUNTED);
    }
}
