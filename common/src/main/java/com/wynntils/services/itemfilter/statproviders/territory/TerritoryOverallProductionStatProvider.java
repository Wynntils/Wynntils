/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders.territory;

import com.wynntils.models.items.items.gui.TerritoryItem;
import java.util.Optional;

public class TerritoryOverallProductionStatProvider extends TerritoryStatProvider<Integer> {
    @Override
    public Optional<Integer> getValue(TerritoryItem territoryItem) {
        return Optional.of(territoryItem.getProduction().values().stream()
                .mapToInt(Integer::intValue)
                .sum());
    }
}
