/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.QualityTierItemProperty;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.List;

public class QualityStatProvider extends ItemStatProvider<Integer> {
    @Override
    public List<Integer> getValue(WynnItem wynnItem) {
        if (!(wynnItem instanceof QualityTierItemProperty qualityTierItemProperty)) return List.of();

        return List.of(qualityTierItemProperty.getQualityTier());
    }

    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }

    @Override
    public List<String> getAliases() {
        return List.of("tier");
    }
}
