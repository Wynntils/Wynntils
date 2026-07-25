/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.dungeonkey;

import com.wynntils.core.components.Services;
import com.wynntils.models.activities.type.Dungeon;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.screens.guides.widgets.filters.GuideFilterWidget;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import java.util.ArrayList;
import java.util.List;

public class DungeonKeyGuideContainerWidget extends GuideContainerWidget<GuideDungeonKeyItemStack> {
    private List<GuideDungeonKeyItemStack> allDungeonKeyItems = new ArrayList<>();

    public DungeonKeyGuideContainerWidget(
            int x, int y, int width, int height, int scrollOffset, boolean displayingFilters) {
        super(x, y, width, height, scrollOffset, displayingFilters);

        rebuildWidgets();
    }

    @Override
    protected GuideButton createGuideButton(int x, int y, GuideDungeonKeyItemStack item) {
        return new DungeonKeyGuideButton(x, y, item);
    }

    @Override
    protected WynntilsGuideScreen.GuideType getGuideType() {
        return WynntilsGuideScreen.GuideType.DUNGEON_KEY;
    }

    @Override
    protected List<GuideFilterWidget> createFilterWidgets(ItemSearchQuery searchQuery) {
        return List.of();
    }

    @Override
    protected List<GuideDungeonKeyItemStack> filterAndSortGuideItems(
            ItemSearchQuery searchQuery, List<GuideDungeonKeyItemStack> guideItems) {
        return Services.ItemFilter.filterAndSort(searchQuery, guideItems);
    }

    @Override
    protected List<GuideDungeonKeyItemStack> getAllGuideItems() {
        if (allDungeonKeyItems.isEmpty()) {
            // Populate list
            for (Dungeon dungeon : Dungeon.values()) {
                if (dungeon.doesExist()) {
                    allDungeonKeyItems.add(new GuideDungeonKeyItemStack(dungeon, false, false));
                    if (dungeon.doesCorruptedExist()) {
                        allDungeonKeyItems.add(new GuideDungeonKeyItemStack(dungeon, false, true));
                    }
                }

                if (dungeon.doesCorruptedExist()) {
                    allDungeonKeyItems.add(new GuideDungeonKeyItemStack(dungeon, true, false));

                    if (dungeon == Dungeon.LOST_SANCTUARY) { // Wynncraft jank... Hopefully forgery gets redone soon
                        allDungeonKeyItems.add(new GuideDungeonKeyItemStack(dungeon, true, true));
                    }
                }
            }
        }

        return allDungeonKeyItems;
    }
}
