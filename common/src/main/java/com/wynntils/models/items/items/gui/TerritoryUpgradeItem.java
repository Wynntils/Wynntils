/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.items.properties.CountedItemProperty;
import com.wynntils.models.territories.type.TerritoryUpgrade;

public class TerritoryUpgradeItem extends GuiItem implements CountedItemProperty {
    private final TerritoryUpgrade territoryUpgrade;
    private final int level;

    public TerritoryUpgradeItem(TerritoryUpgrade territoryUpgrade, int level) {
        this.territoryUpgrade = territoryUpgrade;
        this.level = level;
    }

    public TerritoryUpgrade getTerritoryUpgrade() {
        return territoryUpgrade;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public int getCount() {
        return level;
    }
}
