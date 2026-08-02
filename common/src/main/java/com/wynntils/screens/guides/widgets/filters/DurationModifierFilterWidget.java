/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.wynntils.core.components.Services;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.services.itemfilter.statproviders.DurationModifierStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;

public class DurationModifierFilterWidget extends IntegerStatFilterWidget<DurationModifierStatProvider> {
    public DurationModifierFilterWidget(GuideContainerWidget<?> containerWidget, ItemSearchQuery searchQuery) {
        super(containerWidget, searchQuery, -1500, 500);

        getProvider();
        rebuildWidgets(searchQuery);
    }

    @Override
    public ItemStatProvider<?> getProvider() {
        provider = Services.ItemFilter.getItemStatProviders().stream()
                .filter(statProvider -> statProvider instanceof DurationModifierStatProvider)
                .map(statProvider -> (DurationModifierStatProvider) statProvider)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not get duration modifier stat provider"));
        return provider;
    }
}
