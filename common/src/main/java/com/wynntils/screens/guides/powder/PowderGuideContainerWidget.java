/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.powder;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.screens.guides.widgets.filters.GuideFilterWidget;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import java.util.List;

public class PowderGuideContainerWidget extends GuideContainerWidget<GuidePowderItemStack> {
    private List<GuidePowderItemStack> allPowderItems = List.of();

    public PowderGuideContainerWidget(
            int x, int y, int width, int height, int scrollOffset, boolean displayingFilters) {
        super(x, y, width, height, scrollOffset, displayingFilters);

        rebuildWidgets();
    }

    @Override
    protected GuideButton createGuideButton(int x, int y, GuidePowderItemStack item) {
        return new PowderGuideButton(x, y, item);
    }

    @Override
    protected WynntilsGuideScreen.GuideType getGuideType() {
        return WynntilsGuideScreen.GuideType.POWDER;
    }

    @Override
    protected List<GuideFilterWidget> createFilterWidgets(ItemSearchQuery searchQuery) {
        return List.of();
    }

    @Override
    protected List<GuidePowderItemStack> filterAndSortGuideItems(
            ItemSearchQuery searchQuery, List<GuidePowderItemStack> guideItems) {
        return Services.ItemFilter.filterAndSort(searchQuery, guideItems);
    }

    @Override
    protected List<GuidePowderItemStack> getAllGuideItems() {
        if (allPowderItems.isEmpty()) {
            // Populate list
            allPowderItems = Models.Element.getAllPowderTierInfo().stream()
                    .map(GuidePowderItemStack::new)
                    .toList();
        }

        return allPowderItems;
    }
}
