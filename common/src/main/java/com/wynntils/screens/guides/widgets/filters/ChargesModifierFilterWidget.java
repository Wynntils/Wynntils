/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.wynntils.core.components.Services;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.services.itemfilter.statproviders.ChargesModifierStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;

public class ChargesModifierFilterWidget extends IntegerStatFilterWidget<ChargesModifierStatProvider> {
    public ChargesModifierFilterWidget(GuideContainerWidget<?> containerWidget, ItemSearchQuery searchQuery) {
        super(containerWidget, searchQuery, -5, 5);

        getProvider();
        rebuildWidgets(searchQuery);
    }

    @Override
    public ItemStatProvider<?> getProvider() {
        provider = Services.ItemFilter.getItemStatProviders().stream()
                .filter(statProvider -> statProvider instanceof ChargesModifierStatProvider)
                .map(statProvider -> (ChargesModifierStatProvider) statProvider)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not get charges modifier stat provider"));
        return provider;
    }
}
