/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.QualityTierItemProperty;
import com.wynntils.services.itemfilter.type.ItemFilterType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.List;

public class QualityTierStatProvider extends ItemStatProvider<Integer> {
    @Override
    public List<Integer> getValue(WynnItem wynnItem) {
        if (!(wynnItem instanceof QualityTierItemProperty qualityTierItemProperty)) return List.of();

        return List.of(qualityTierItemProperty.getQualityTier());
    }

    @Override
    public List<ItemFilterType> getFilterTypes() {
        return List.of(ItemFilterType.INGREDIENT, ItemFilterType.MATERIAL);
    }
}
