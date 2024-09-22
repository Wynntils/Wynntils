/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders.territory;

import com.wynntils.models.items.items.gui.TerritoryItem;
import java.util.List;

public class TerritoryTreasuryStatProvider extends TerritoryStatProvider<Integer> {
    @Override
    public List<Integer> getValue(TerritoryItem territoryItem) {
        return territoryItem.getTreasuryBonus() == -1 ? List.of() : List.of(territoryItem.getTreasuryBonus());
    }
}
