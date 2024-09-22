/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.territorymanagement.widgets.quickfilters;

import com.wynntils.screens.territorymanagement.TerritoryManagementScreen;
import com.wynntils.services.itemfilter.filters.RangedStatFilters;
import com.wynntils.services.itemfilter.statproviders.territory.TerritoryUpgradeCountStatProvider;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TerritoryBonusesQuickFilterWidget extends TerritoryToggleQuickFilterWidget {
    public TerritoryBonusesQuickFilterWidget(int x, int y, int width, int height, TerritoryManagementScreen screen) {
        super(x, y, width, height, screen);
    }

    @Override
    protected MutableComponent getFilterName() {
        String toggleString = filterToggle == null ? "-" : (filterToggle ? "✔" : "✖");
        return Component.literal("Any Upgrades: %s".formatted(toggleString));
    }

    @Override
    protected CustomColor getFilterColor() {
        return CommonColors.WHITE;
    }

    @Override
    protected List<StatProviderAndFilterPair> getFilters() {
        if (filterToggle == null) return List.of();

        return List.of(new StatProviderAndFilterPair(
                new TerritoryUpgradeCountStatProvider(),
                new RangedStatFilters.RangedIntegerStatFilter.RangedIntegerStatFilterFactory()
                        .create(filterToggle ? ">0" : "0")
                        .orElseThrow()));
    }
}
