/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.sets;

import com.wynntils.core.components.Models;
import com.wynntils.models.gear.type.SetInfo;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.screens.guides.widgets.filters.GuideFilterWidget;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;

public class SetsGuideContainerWidget extends GuideContainerWidget<SetInfo> {
    private List<SetInfo> allSets = new ArrayList<>();

    public SetsGuideContainerWidget(int x, int y, int width, int height, int scrollOffset, boolean displayingFilters) {
        super(x, y, width, height, scrollOffset, displayingFilters);

        rebuildWidgets();
    }

    @Override
    protected boolean hasFavoriteFilter() {
        return false;
    }

    @Override
    protected GuideButton createGuideButton(int x, int y, SetInfo item) {
        return new SetGuideButton(x, y, item);
    }

    @Override
    protected int getWidgetWidth() {
        return 82;
    }

    @Override
    protected WynntilsGuideScreen.GuideType getGuideType() {
        return WynntilsGuideScreen.GuideType.SETS;
    }

    @Override
    protected List<GuideFilterWidget> createFilterWidgets(ItemSearchQuery searchQuery) {
        return List.of();
    }

    @Override
    protected List<SetInfo> filterAndSortGuideItems(ItemSearchQuery searchQuery, List<SetInfo> guideItems) {
        String nameSearch = String.join(" ", searchQuery.plainTextTokens());
        if (nameSearch.isBlank()) return allSets;

        return allSets.stream()
                .filter(setInfo -> StringUtils.partialMatch(setInfo.name(), nameSearch))
                .toList();
    }

    @Override
    protected List<SetInfo> getAllGuideItems() {
        if (allSets.isEmpty()) {
            // Populate list
            allSets = Models.Set.getSets().stream().toList();
        }

        return allSets;
    }
}
