/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders;

import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.properties.GearTierItemProperty;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.List;

public class RarityStatProvider extends ItemStatProvider<String> {
    @Override
    public List<String> getValue(WynnItem wynnItem) {
        if (!(wynnItem instanceof GearTierItemProperty gearTierItemProperty)) return List.of();

        return List.of(gearTierItemProperty.getGearTier().getName());
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

    @Override
    public List<String> getAliases() {
        return List.of();
    }
}
