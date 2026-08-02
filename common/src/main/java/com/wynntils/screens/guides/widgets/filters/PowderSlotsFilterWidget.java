/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.wynntils.core.components.Services;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.services.itemfilter.statproviders.PowderSlotsStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;

public class PowderSlotsFilterWidget extends IntegerStatFilterWidget<PowderSlotsStatProvider> {
    public PowderSlotsFilterWidget(GuideContainerWidget<?> containerWidget, ItemSearchQuery searchQuery) {
        super(containerWidget, searchQuery, 0, 25);

        getProvider();
        rebuildWidgets(searchQuery);
    }

    @Override
    public ItemStatProvider<?> getProvider() {
        provider = Services.ItemFilter.getItemStatProviders().stream()
                .filter(statProvider -> statProvider instanceof PowderSlotsStatProvider)
                .map(statProvider -> (PowderSlotsStatProvider) statProvider)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not get powder slots stat provider"));
        return provider;
    }
}
