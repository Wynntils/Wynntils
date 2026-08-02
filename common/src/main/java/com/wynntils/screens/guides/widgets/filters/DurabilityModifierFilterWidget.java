/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.wynntils.core.components.Services;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.services.itemfilter.statproviders.DurabilityModifierStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;

public class DurabilityModifierFilterWidget extends IntegerStatFilterWidget<DurabilityModifierStatProvider> {
    public DurabilityModifierFilterWidget(GuideContainerWidget<?> containerWidget, ItemSearchQuery searchQuery) {
        super(containerWidget, searchQuery, -250, 100);

        getProvider();
        rebuildWidgets(searchQuery);
    }

    @Override
    public ItemStatProvider<?> getProvider() {
        provider = Services.ItemFilter.getItemStatProviders().stream()
                .filter(statProvider -> statProvider instanceof DurabilityModifierStatProvider)
                .map(statProvider -> (DurabilityModifierStatProvider) statProvider)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not get durability modifier stat provider"));
        return provider;
    }
}
