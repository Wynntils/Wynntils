/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.emerald;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.screens.guides.widgets.filters.GuideFilterWidget;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import java.util.ArrayList;
import java.util.List;

public class EmeraldGuideContainerWidget extends GuideContainerWidget<GuideEmeraldItemStack> {
    private List<GuideEmeraldItemStack> allEmeraldItems = new ArrayList<>();

    public EmeraldGuideContainerWidget(
            int x, int y, int width, int height, int scrollOffset, boolean displayingFilters) {
        super(x, y, width, height, scrollOffset, displayingFilters);

        rebuildWidgets();
    }

    @Override
    protected GuideButton createGuideButton(int x, int y, GuideEmeraldItemStack item) {
        return new EmeraldGuideButton(x, y, item);
    }

    @Override
    protected WynntilsGuideScreen.GuideType getGuideType() {
        return WynntilsGuideScreen.GuideType.EMERALD;
    }

    @Override
    protected List<GuideFilterWidget> createFilterWidgets(ItemSearchQuery searchQuery) {
        return List.of();
    }

    @Override
    protected List<GuideEmeraldItemStack> filterAndSortGuideItems(
            ItemSearchQuery searchQuery, List<GuideEmeraldItemStack> guideItems) {
        return Services.ItemFilter.filterAndSort(searchQuery, guideItems);
    }

    @Override
    protected List<GuideEmeraldItemStack> getAllGuideItems() {
        if (allEmeraldItems.isEmpty()) {
            // Populate list
            allEmeraldItems.addAll(Models.Emerald.getAllEmeraldItems());

            for (int i = 1; i <= 10; i++) {
                allEmeraldItems.add(new GuideEmeraldPouchItemStack(i));
            }
        }

        return allEmeraldItems;
    }
}
