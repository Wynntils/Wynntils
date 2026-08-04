/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.rune;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.models.rewards.type.RuneType;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.screens.guides.widgets.filters.GuideFilterWidget;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import java.util.ArrayList;
import java.util.List;

public class RuneGuideContainerWidget extends GuideContainerWidget<GuideRuneItemStack> {
    private List<GuideRuneItemStack> allRuneItems = new ArrayList<>();

    public RuneGuideContainerWidget(int x, int y, int width, int height, int scrollOffset, boolean displayingFilters) {
        super(x, y, width, height, scrollOffset, displayingFilters);

        rebuildWidgets();
    }

    @Override
    protected GuideButton createGuideButton(int x, int y, GuideRuneItemStack item) {
        return new RuneGuideButton(x, y, item);
    }

    @Override
    protected WynntilsGuideScreen.GuideType getGuideType() {
        return WynntilsGuideScreen.GuideType.RUNE;
    }

    @Override
    protected List<GuideFilterWidget> createFilterWidgets(ItemSearchQuery searchQuery) {
        return List.of();
    }

    @Override
    protected List<GuideRuneItemStack> filterAndSortGuideItems(
            ItemSearchQuery searchQuery, List<GuideRuneItemStack> guideItems) {
        return Services.ItemFilter.filterAndSort(searchQuery, guideItems);
    }

    @Override
    protected List<GuideRuneItemStack> getAllGuideItems() {
        if (allRuneItems.isEmpty()) {
            // Populate list
            for (RuneType runeType : Models.Rewards.getAllRuneInfo()) {
                allRuneItems.add(new GuideRuneItemStack(runeType));
            }
        }

        return allRuneItems;
    }
}
