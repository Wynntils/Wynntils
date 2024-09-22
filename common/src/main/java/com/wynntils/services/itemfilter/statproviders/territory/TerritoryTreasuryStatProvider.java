/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders.territory;

import com.wynntils.models.items.items.gui.TerritoryItem;
import java.util.Optional;

public class TerritoryTreasuryStatProvider extends TerritoryStatProvider<Integer> {
    @Override
    public Optional<Integer> getValue(TerritoryItem territoryItem) {
        // We don't support float stat providers, so we cast to int
        return territoryItem.getTreasuryBonus() == -1
                ? Optional.empty()
                : Optional.of((int) territoryItem.getTreasuryBonus());
    }
}
