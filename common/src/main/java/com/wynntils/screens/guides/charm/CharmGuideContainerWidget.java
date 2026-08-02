/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.charm;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.screens.guides.widgets.filters.GuideFilterWidget;
import com.wynntils.screens.guides.widgets.filters.LevelFilterWidget;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import java.util.Comparator;
import java.util.List;

public class CharmGuideContainerWidget extends GuideContainerWidget<GuideCharmItemStack> {
    private List<GuideCharmItemStack> allCharmItems = List.of();

    public CharmGuideContainerWidget(int x, int y, int width, int height, int scrollOffset, boolean displayingFilters) {
        super(x, y, width, height, scrollOffset, displayingFilters);

        rebuildWidgets();
    }

    @Override
    protected GuideButton createGuideButton(int x, int y, GuideCharmItemStack item) {
        return new CharmGuideButton(x, y, item);
    }

    @Override
    protected WynntilsGuideScreen.GuideType getGuideType() {
        return WynntilsGuideScreen.GuideType.CHARMS;
    }

    @Override
    protected List<GuideFilterWidget> createFilterWidgets(ItemSearchQuery searchQuery) {
        return List.of(new LevelFilterWidget(this, searchQuery, true));
    }

    @Override
    protected List<GuideCharmItemStack> filterAndSortGuideItems(
            ItemSearchQuery searchQuery, List<GuideCharmItemStack> guideItems) {
        return Services.ItemFilter.filterAndSort(searchQuery, guideItems);
    }

    @Override
    protected List<GuideCharmItemStack> getAllGuideItems() {
        if (allCharmItems.isEmpty()) {
            // Populate list
            allCharmItems = Models.Rewards.getAllCharmInfos()
                    .sorted(Comparator.comparingInt(
                            charmInfo -> charmInfo.requirements().level()))
                    .map(GuideCharmItemStack::new)
                    .toList();
        }

        return allCharmItems;
    }
}
