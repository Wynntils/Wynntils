/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.ingredient;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.models.ingredients.type.IngredientPosition;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.screens.guides.widgets.filters.ChargesModifierFilterWidget;
import com.wynntils.screens.guides.widgets.filters.DurabilityModifierFilterWidget;
import com.wynntils.screens.guides.widgets.filters.DurationModifierFilterWidget;
import com.wynntils.screens.guides.widgets.filters.GuideFilterWidget;
import com.wynntils.screens.guides.widgets.filters.IngredientEffectivenessFilterWidget;
import com.wynntils.screens.guides.widgets.filters.IntegerStatFilterWidget;
import com.wynntils.screens.guides.widgets.filters.LevelFilterWidget;
import com.wynntils.screens.guides.widgets.filters.ProfessionTypeFilterWidget;
import com.wynntils.screens.guides.widgets.filters.QualityTierFilterWidget;
import com.wynntils.services.itemfilter.statproviders.ActualStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import java.util.ArrayList;
import java.util.List;

public class IngredientGuideContainerWidget extends GuideContainerWidget<GuideIngredientItemStack> {
    private List<GuideIngredientItemStack> allIngredientItems = List.of();

    public IngredientGuideContainerWidget(
            int x, int y, int width, int height, int scrollOffset, boolean displayingFilters) {
        super(x, y, width, height, scrollOffset, displayingFilters);

        rebuildWidgets();
    }

    @Override
    protected GuideButton createGuideButton(int x, int y, GuideIngredientItemStack item) {
        return new IngredientGuideButton(x, y, item);
    }

    @Override
    protected WynntilsGuideScreen.GuideType getGuideType() {
        return WynntilsGuideScreen.GuideType.INGREDIENTS;
    }

    @Override
    protected List<GuideFilterWidget> createFilterWidgets(ItemSearchQuery searchQuery) {
        List<GuideFilterWidget> filterWidgets = new ArrayList<>(List.of(
                new ProfessionTypeFilterWidget(this, searchQuery, false),
                new QualityTierFilterWidget(this, searchQuery),
                new LevelFilterWidget(this, searchQuery, false),
                new DurabilityModifierFilterWidget(this, searchQuery),
                new DurationModifierFilterWidget(this, searchQuery),
                new ChargesModifierFilterWidget(this, searchQuery)));

        for (IngredientPosition position : IngredientPosition.values()) {
            filterWidgets.add(new IngredientEffectivenessFilterWidget(position, this, searchQuery));
        }

        Services.ItemFilter.getItemStatProviders().stream()
                .filter(provider -> provider instanceof ActualStatProvider)
                .map(provider -> IntegerStatFilterWidget.createStatValue(this, searchQuery, provider))
                .forEach(filterWidgets::add);

        return filterWidgets;
    }

    @Override
    protected List<GuideIngredientItemStack> filterAndSortGuideItems(
            ItemSearchQuery searchQuery, List<GuideIngredientItemStack> guideItems) {
        return Services.ItemFilter.filterAndSort(searchQuery, guideItems);
    }

    @Override
    protected List<GuideIngredientItemStack> getAllGuideItems() {
        if (allIngredientItems.isEmpty()) {
            allIngredientItems = Models.Ingredient.getAllIngredientInfos()
                    .map(GuideIngredientItemStack::new)
                    .toList();
        }

        return allIngredientItems;
    }
}
