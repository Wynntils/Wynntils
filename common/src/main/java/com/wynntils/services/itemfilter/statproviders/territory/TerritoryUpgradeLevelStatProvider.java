/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders.territory;

import com.google.common.base.CaseFormat;
import com.wynntils.models.items.items.gui.TerritoryItem;
import com.wynntils.models.territories.type.TerritoryUpgrade;
import java.util.List;

public class TerritoryUpgradeLevelStatProvider extends TerritoryStatProvider<Integer> {
    private final TerritoryUpgrade territoryUpgrade;

    public TerritoryUpgradeLevelStatProvider(TerritoryUpgrade territoryUpgrade) {
        this.territoryUpgrade = territoryUpgrade;
    }

    @Override
    public List<Integer> getValue(TerritoryItem territoryItem) {
        Integer upgradeLevel = territoryItem.getUpgrades().get(territoryUpgrade);
        return upgradeLevel == null ? List.of() : List.of(upgradeLevel);
    }

    @Override
    public String getName() {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, territoryUpgrade.name());
    }

    @Override
    public String getDescription() {
        return getTranslation("description", territoryUpgrade.getName());
    }
}
