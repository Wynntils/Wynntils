/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.sorts;

import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.services.itemfilter.statproviders.RarityStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.SortInfo;
import java.util.Optional;
import net.minecraft.client.resources.language.I18n;

public class RaritySortButton extends GuideSortButton {
    public RaritySortButton(WynntilsGuideScreen guideScreen, ItemSearchQuery searchQuery) {
        super(guideScreen);

        updateFromQuery(searchQuery);
    }

    public void updateFromQuery(ItemSearchQuery searchQuery) {
        Optional<SortInfo> sortInfoOptional = searchQuery.sorts().stream()
                .filter(sortInfo -> sortInfo.provider() instanceof RarityStatProvider)
                .findFirst();

        sortDirection = sortInfoOptional.map(SortInfo::direction).orElse(null);
    }

    @Override
    protected SortInfo getSortInfo() {
        if (sortDirection == null) return null;

        return new SortInfo(sortDirection, new RarityStatProvider());
    }

    @Override
    protected String getSortName() {
        return I18n.get("service.wynntils.itemFilter.stat.rarity.name");
    }
}
