/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders.territory;

import com.google.common.base.CaseFormat;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.gui.TerritoryItem;
import com.wynntils.services.itemfilter.type.ItemProviderType;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.List;
import java.util.Optional;

abstract class TerritoryStatProvider<T extends Comparable<T>> extends ItemStatProvider<T> {
    @Override
    public final Optional<T> getValue(WynnItem wynnItem) {
        if (wynnItem instanceof TerritoryItem territoryItem) {
            return getValue(territoryItem);
        }

        return Optional.empty();
    }

    public abstract Optional<T> getValue(TerritoryItem territoryItem);

    @Override
    public List<ItemProviderType> getFilterTypes() {
        return List.of(ItemProviderType.TERRITORY);
    }

    @Override
    public String getName() {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, super.getName().replaceFirst("territory", ""));
    }
}
