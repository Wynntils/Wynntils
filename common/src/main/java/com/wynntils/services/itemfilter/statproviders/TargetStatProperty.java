/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.TargetedItemProperty;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.List;

public class TargetStatProperty extends ItemStatProvider<String> {
    @Override
    public List<String> getValue(WynnItem wynnItem) {
        if (!(wynnItem instanceof TargetedItemProperty targetedItemProperty)) return List.of();

        return List.of(targetedItemProperty.getTarget());
    }

    @Override
    public List<ItemProviderType> getFilterTypes() {
        return List.of(ItemProviderType.GENERIC);
    }
}
