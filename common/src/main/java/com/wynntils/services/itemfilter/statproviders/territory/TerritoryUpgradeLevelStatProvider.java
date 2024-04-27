/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.statproviders.territory;

import com.google.common.base.CaseFormat;
import com.wynntils.models.items.items.gui.TerritoryItem;
import com.wynntils.models.territories.type.TerritoryUpgrade;
import java.util.Optional;

public class TerritoryUpgradeLevelStatProvider extends TerritoryStatProvider<Integer> {
    private final TerritoryUpgrade territoryUpgrade;

    public TerritoryUpgradeLevelStatProvider(TerritoryUpgrade territoryUpgrade) {
        this.territoryUpgrade = territoryUpgrade;
    }

    @Override
    public Optional<Integer> getValue(TerritoryItem territoryItem) {
        return Optional.ofNullable(territoryItem.getUpgrades().get(territoryUpgrade));
    }

    @Override
    public String getName() {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, territoryUpgrade.name());
    }

    @Override
    public String getDisplayName() {
        return territoryUpgrade.getName() + " Level";
    }

    @Override
    public String getDescription() {
        return getTranslation("description", territoryUpgrade.getName());
    }
}
