/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.material;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;
import com.wynntils.models.profession.type.MaterialInfo;
import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.screens.guides.widgets.GuideButton;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.screens.guides.widgets.filters.GuideFilterWidget;
import com.wynntils.screens.guides.widgets.filters.LevelFilterWidget;
import com.wynntils.screens.guides.widgets.filters.ProfessionTypeFilterWidget;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public class MaterialGuideContainerWidget extends GuideContainerWidget<GuideMaterialItemStack> {
    private List<GuideMaterialItemStack> allMaterialItems = List.of();

    public MaterialGuideContainerWidget(
            int x, int y, int width, int height, int scrollOffset, boolean displayingFilters) {
        super(x, y, width, height, scrollOffset, displayingFilters);

        rebuildWidgets();
    }

    @Override
    protected GuideButton createGuideButton(int x, int y, GuideMaterialItemStack item) {
        return new MaterialGuideButton(x, y, item);
    }

    @Override
    protected WynntilsGuideScreen.GuideType getGuideType() {
        return WynntilsGuideScreen.GuideType.MATERIALS;
    }

    @Override
    protected List<GuideFilterWidget> createFilterWidgets(ItemSearchQuery searchQuery) {
        return List.of(
                new ProfessionTypeFilterWidget(this, searchQuery, true),
                new LevelFilterWidget(this, searchQuery, false));
    }

    @Override
    protected List<GuideMaterialItemStack> filterAndSortGuideItems(
            ItemSearchQuery searchQuery, List<GuideMaterialItemStack> guideItems) {
        return Services.ItemFilter.filterAndSort(searchQuery, guideItems);
    }

    @Override
    protected List<GuideMaterialItemStack> getAllGuideItems() {
        if (allMaterialItems.isEmpty()) {
            allMaterialItems = Models.Profession.getAllMaterialInfos()
                    .sorted(Comparator.comparing(MaterialInfo::professionType).thenComparingInt(MaterialInfo::level))
                    .flatMap(materialInfo -> IntStream.rangeClosed(1, 3)
                            .mapToObj(tier -> new GuideMaterialItemStack(materialInfo, tier)))
                    .toList();
        }

        return allMaterialItems;
    }
}
