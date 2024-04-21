/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.items.WynnItem;
import com.wynntils.services.itemfilter.type.ItemFilterType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.List;

public class ItemTypeStatProvider extends ItemStatProvider<String> {
    @Override
    public List<String> getValue(WynnItem wynnItem) {
        return List.of(wynnItem.getClass().getSimpleName().replace("Item", ""));
    }

    @Override
    public List<ItemFilterType> getFilterTypes() {
        return List.of(ItemFilterType.GENERIC);
    }
}
