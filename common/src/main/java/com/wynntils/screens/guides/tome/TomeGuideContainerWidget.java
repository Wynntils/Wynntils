/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.tome;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.models.rewards.type.TomeInfo;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.screens.guides.widgets.filters.GearTierFilterWidget;
import com.wynntils.screens.guides.widgets.filters.GuideFilterWidget;
import com.wynntils.screens.guides.widgets.filters.LevelFilterWidget;
import com.wynntils.screens.guides.widgets.filters.TomeTypeFilterWidget;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import java.util.Comparator;
import java.util.List;

public class TomeGuideContainerWidget extends GuideContainerWidget<GuideTomeItemStack> {
    private List<GuideTomeItemStack> allTomeItems = List.of();

    public TomeGuideContainerWidget(int x, int y, int width, int height, int scrollOffset, boolean displayingFilters) {
        super(x, y, width, height, scrollOffset, displayingFilters);

        rebuildWidgets();
    }

    @Override
    protected GuideButton createGuideButton(int x, int y, GuideTomeItemStack item) {
        return new TomeGuideButton(x, y, item);
    }

    @Override
    protected WynntilsGuideScreen.GuideType getGuideType() {
        return WynntilsGuideScreen.GuideType.TOMES;
    }

    @Override
    protected List<GuideFilterWidget> createFilterWidgets(ItemSearchQuery searchQuery) {
        return List.of(
                new TomeTypeFilterWidget(this, searchQuery),
                new GearTierFilterWidget(this, searchQuery),
                new LevelFilterWidget(this, searchQuery, true));
    }

    @Override
    protected List<GuideTomeItemStack> filterAndSortGuideItems(
            ItemSearchQuery searchQuery, List<GuideTomeItemStack> guideItems) {
        return Services.ItemFilter.filterAndSort(searchQuery, guideItems);
    }

    @Override
    protected List<GuideTomeItemStack> getAllGuideItems() {
        if (allTomeItems.isEmpty()) {
            // Populate list
            allTomeItems = Models.Rewards.getAllTomeInfos()
                    .sorted(Comparator.comparing(TomeInfo::type)
                            .thenComparing(TomeInfo::tier)
                            .thenComparingInt(
                                    tomeInfo -> tomeInfo.requirements().level()))
                    .map(GuideTomeItemStack::new)
                    .toList();
        }

        return allTomeItems;
    }
}
