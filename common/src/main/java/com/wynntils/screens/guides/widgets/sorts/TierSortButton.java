/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.sorts;

import com.wynntils.screens.guides.WynntilsGuideScreen;
import com.wynntils.services.itemfilter.statproviders.TierStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.SortInfo;
import java.util.Optional;
import net.minecraft.client.resources.language.I18n;

public class TierSortButton extends GuideSortButton {
    public TierSortButton(WynntilsGuideScreen guideScreen, ItemSearchQuery searchQuery) {
        super(guideScreen);

        updateFromQuery(searchQuery);
    }

    public void updateFromQuery(ItemSearchQuery searchQuery) {
        Optional<SortInfo> sortInfoOptional = searchQuery.sorts().stream()
                .filter(sortInfo -> sortInfo.provider() instanceof TierStatProvider)
                .findFirst();

        sortDirection = sortInfoOptional.map(SortInfo::direction).orElse(null);
    }

    @Override
    protected SortInfo getSortInfo() {
        if (sortDirection == null) return null;

        return new SortInfo(sortDirection, new TierStatProvider());
    }

    @Override
    protected String getSortName() {
        return I18n.get("service.wynntils.itemFilter.stat.tier.name");
    }
}
