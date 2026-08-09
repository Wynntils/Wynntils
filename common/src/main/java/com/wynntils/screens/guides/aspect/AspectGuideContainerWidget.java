/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.aspect;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.screens.guides.widgets.filters.ClassTypeFilterWidget;
import com.wynntils.screens.guides.widgets.filters.GearTierFilterWidget;
import com.wynntils.screens.guides.widgets.filters.GuideFilterWidget;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AspectGuideContainerWidget extends GuideContainerWidget<GuideAspectItemStack> {
    private List<GuideAspectItemStack> allAspectItems = List.of();

    public AspectGuideContainerWidget(
            int x, int y, int width, int height, int scrollOffset, boolean displayingFilters) {
        super(x, y, width, height, scrollOffset, displayingFilters);

        rebuildWidgets();
    }

    @Override
    protected GuideButton createGuideButton(int x, int y, GuideAspectItemStack item) {
        return new AspectGuideButton(x, y, item);
    }

    @Override
    protected WynntilsGuideScreen.GuideType getGuideType() {
        return WynntilsGuideScreen.GuideType.ASPECTS;
    }

    @Override
    protected List<GuideFilterWidget> createFilterWidgets(ItemSearchQuery searchQuery) {
        return List.of(new ClassTypeFilterWidget(this, searchQuery), new GearTierFilterWidget(this, searchQuery));
    }

    @Override
    protected List<GuideAspectItemStack> filterAndSortGuideItems(
            ItemSearchQuery searchQuery, List<GuideAspectItemStack> guideItems) {
        return Services.ItemFilter.filterAndSort(searchQuery, guideItems);
    }

    @Override
    protected List<GuideAspectItemStack> getAllGuideItems() {
        if (allAspectItems.isEmpty()) {
            // Populate list, create an item for each tier
            allAspectItems = Models.Aspect.getAllAspectInfos()
                    .flatMap(aspectInfo -> IntStream.range(
                                    0, aspectInfo.effects().size())
                            .mapToObj(tier -> new GuideAspectItemStack(aspectInfo, tier + 1)))
                    .collect(Collectors.toList());
        }

        return allAspectItems;
    }
}
