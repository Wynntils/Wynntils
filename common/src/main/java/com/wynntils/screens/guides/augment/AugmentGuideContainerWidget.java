/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.augment;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.models.rewards.type.AmplifierInfo;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.screens.guides.widgets.filters.GuideFilterWidget;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import java.util.ArrayList;
import java.util.List;

public class AugmentGuideContainerWidget extends GuideContainerWidget<GuideAugmentItemStack> {
    private List<GuideAugmentItemStack> allAugmentItems = new ArrayList<>();

    public AugmentGuideContainerWidget(
            int x, int y, int width, int height, int scrollOffset, boolean displayingFilters) {
        super(x, y, width, height, scrollOffset, displayingFilters);

        rebuildWidgets();
    }

    @Override
    protected GuideButton createGuideButton(int x, int y, GuideAugmentItemStack item) {
        return new AugmentGuideButton(x, y, item);
    }

    @Override
    protected WynntilsGuideScreen.GuideType getGuideType() {
        return WynntilsGuideScreen.GuideType.AUGMENTS;
    }

    @Override
    protected List<GuideFilterWidget> createFilterWidgets(ItemSearchQuery searchQuery) {
        return List.of();
    }

    @Override
    protected List<GuideAugmentItemStack> filterAndSortGuideItems(
            ItemSearchQuery searchQuery, List<GuideAugmentItemStack> guideItems) {
        return Services.ItemFilter.filterAndSort(searchQuery, guideItems);
    }

    @Override
    protected List<GuideAugmentItemStack> getAllGuideItems() {
        if (allAugmentItems.isEmpty()) {
            // Populate list
            for (AmplifierInfo amplifierInfo : Models.Rewards.getAllAmplifierInfo()) {
                allAugmentItems.add(new GuideAmplifierItemStack(amplifierInfo));
            }

            allAugmentItems.add(new GuideSimulatorItemStack());
            allAugmentItems.add(new GuideInsulatorItemStack());
        }

        return allAugmentItems;
    }
}
