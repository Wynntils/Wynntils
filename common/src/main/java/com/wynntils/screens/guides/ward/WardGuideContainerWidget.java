/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.ward;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.models.rewards.type.WardType;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.screens.guides.widgets.filters.GuideFilterWidget;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import java.util.ArrayList;
import java.util.List;

public class WardGuideContainerWidget extends GuideContainerWidget<GuideWardItemStack> {
    private List<GuideWardItemStack> allWardItems = new ArrayList<>();

    public WardGuideContainerWidget(int x, int y, int width, int height, int scrollOffset, boolean displayingFilters) {
        super(x, y, width, height, scrollOffset, displayingFilters);

        rebuildWidgets();
    }

    @Override
    protected GuideButton createGuideButton(int x, int y, GuideWardItemStack item) {
        return new WardGuideButton(x, y, item);
    }

    @Override
    protected WynntilsGuideScreen.GuideType getGuideType() {
        return WynntilsGuideScreen.GuideType.WARDS;
    }

    @Override
    protected List<GuideFilterWidget> createFilterWidgets(ItemSearchQuery searchQuery) {
        return List.of();
    }

    @Override
    protected List<GuideWardItemStack> filterAndSortGuideItems(
            ItemSearchQuery searchQuery, List<GuideWardItemStack> guideItems) {
        return Services.ItemFilter.filterAndSort(searchQuery, guideItems);
    }

    @Override
    protected List<GuideWardItemStack> getAllGuideItems() {
        if (allWardItems.isEmpty()) {
            // Populate list
            for (WardType wardType : Models.Rewards.getAllWardInfo()) {
                allWardItems.add(new GuideWardItemStack(wardType));
            }
        }

        return allWardItems;
    }
}
