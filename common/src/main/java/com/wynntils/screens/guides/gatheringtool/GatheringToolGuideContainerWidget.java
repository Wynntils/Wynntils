/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.gatheringtool;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.models.profession.type.GatheringToolInfo;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.screens.guides.widgets.filters.GearTierFilterWidget;
import com.wynntils.screens.guides.widgets.filters.GuideFilterWidget;
import com.wynntils.screens.guides.widgets.filters.LevelFilterWidget;
import com.wynntils.screens.guides.widgets.filters.ProfessionTypeFilterWidget;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import java.util.Comparator;
import java.util.List;

public class GatheringToolGuideContainerWidget extends GuideContainerWidget<GuideGatheringToolItemStack> {
    private List<GuideGatheringToolItemStack> allToolItems = List.of();

    public GatheringToolGuideContainerWidget(
            int x, int y, int width, int height, int scrollOffset, boolean displayingFilters) {
        super(x, y, width, height, scrollOffset, displayingFilters);

        rebuildWidgets();
    }

    @Override
    protected GuideButton createGuideButton(int x, int y, GuideGatheringToolItemStack item) {
        return new GatheringToolGuideButton(x, y, item);
    }

    @Override
    protected WynntilsGuideScreen.GuideType getGuideType() {
        return WynntilsGuideScreen.GuideType.TOOLS;
    }

    @Override
    protected List<GuideFilterWidget> createFilterWidgets(ItemSearchQuery searchQuery) {
        return List.of(
                new ProfessionTypeFilterWidget(this, searchQuery, true),
                new GearTierFilterWidget(this, searchQuery),
                new LevelFilterWidget(this, searchQuery, false));
    }

    @Override
    protected List<GuideGatheringToolItemStack> filterAndSortGuideItems(
            ItemSearchQuery searchQuery, List<GuideGatheringToolItemStack> guideItems) {
        return Services.ItemFilter.filterAndSort(searchQuery, guideItems);
    }

    @Override
    protected List<GuideGatheringToolItemStack> getAllGuideItems() {
        if (allToolItems.isEmpty()) {
            allToolItems = Models.Profession.getAllGatheringToolInfos()
                    .sorted(Comparator.comparingInt(GatheringToolInfo::level))
                    .map(GuideGatheringToolItemStack::new)
                    .toList();
        }

        return allToolItems;
    }
}
