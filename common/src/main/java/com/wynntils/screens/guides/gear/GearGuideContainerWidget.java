/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.gear;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.screens.guides.widgets.filters.AttackSpeedFilterWidget;
import com.wynntils.screens.guides.widgets.filters.GearRestrictionFilterWidget;
import com.wynntils.screens.guides.widgets.filters.GearTierFilterWidget;
import com.wynntils.screens.guides.widgets.filters.GearTypeFilterWidget;
import com.wynntils.screens.guides.widgets.filters.GuideFilterWidget;
import com.wynntils.screens.guides.widgets.filters.IntegerStatFilterWidget;
import com.wynntils.screens.guides.widgets.filters.LevelFilterWidget;
import com.wynntils.screens.guides.widgets.filters.MajorIdFilterWidget;
import com.wynntils.screens.guides.widgets.filters.PowderSlotsFilterWidget;
import com.wynntils.services.itemfilter.statproviders.ActualStatProvider;
import com.wynntils.services.itemfilter.statproviders.AverageDpsStatProvider;
import com.wynntils.services.itemfilter.statproviders.HealthStatProvider;
import com.wynntils.services.itemfilter.statproviders.SkillReqStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GearGuideContainerWidget extends GuideContainerWidget<GuideGearItemStack> {
    private List<GuideGearItemStack> allGearItems = List.of();

    public GearGuideContainerWidget(int x, int y, int width, int height, int scrollOffset, boolean displayingFilters) {
        super(x, y, width, height, scrollOffset, displayingFilters);

        rebuildWidgets();
    }

    @Override
    protected GuideButton createGuideButton(int x, int y, GuideGearItemStack item) {
        return new GearGuideButton(x, y, item);
    }

    @Override
    protected WynntilsGuideScreen.GuideType getGuideType() {
        return WynntilsGuideScreen.GuideType.GEAR;
    }

    @Override
    protected List<GuideFilterWidget> createFilterWidgets(ItemSearchQuery searchQuery) {
        List<GuideFilterWidget> filterWidgets = new ArrayList<>(List.of(
                new GearTierFilterWidget(this, searchQuery),
                new GearTypeFilterWidget(this, searchQuery),
                new LevelFilterWidget(this, searchQuery, true),
                new GearRestrictionFilterWidget(this, searchQuery),
                new AttackSpeedFilterWidget(this, searchQuery),
                new PowderSlotsFilterWidget(this, searchQuery)));
        MajorIdFilterWidget.create(this, searchQuery).ifPresent(filterWidgets::add);

        Services.ItemFilter.getItemStatProviders().stream()
                .map(provider -> createStatFilterWidget(searchQuery, provider))
                .flatMap(Optional::stream)
                .forEach(filterWidgets::add);

        return filterWidgets;
    }

    private Optional<GuideFilterWidget> createStatFilterWidget(
            ItemSearchQuery searchQuery, ItemStatProvider<?> provider) {
        if (provider instanceof AverageDpsStatProvider
                || provider instanceof HealthStatProvider
                || provider instanceof SkillReqStatProvider) {
            return Optional.of(IntegerStatFilterWidget.create(this, searchQuery, provider));
        } else if (provider instanceof ActualStatProvider) {
            return Optional.of(IntegerStatFilterWidget.createStatValue(this, searchQuery, provider));
        }

        return Optional.empty();
    }

    @Override
    protected List<GuideGearItemStack> filterAndSortGuideItems(
            ItemSearchQuery searchQuery, List<GuideGearItemStack> guideItems) {
        return Services.ItemFilter.filterAndSort(searchQuery, guideItems);
    }

    @Override
    protected List<GuideGearItemStack> getAllGuideItems() {
        if (allGearItems.isEmpty()) {
            // Populate list
            allGearItems =
                    Models.Gear.getAllGearInfos().map(GuideGearItemStack::new).toList();
        }

        return allGearItems;
    }
}
