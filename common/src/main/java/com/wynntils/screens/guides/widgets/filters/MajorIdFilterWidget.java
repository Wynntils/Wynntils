/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.wynntils.core.components.Services;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.services.itemfilter.statproviders.MajorIdStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import java.util.Optional;

public class MajorIdFilterWidget extends AnyStatGuideFilterWidget<MajorIdStatProvider> {
    private MajorIdFilterWidget(
            GuideContainerWidget<?> containerWidget, ItemSearchQuery searchQuery, MajorIdStatProvider provider) {
        super(containerWidget, searchQuery, provider);
    }

    public static Optional<MajorIdFilterWidget> create(
            GuideContainerWidget<?> containerWidget, ItemSearchQuery searchQuery) {
        return Services.ItemFilter.getItemStatProviders().stream()
                .filter(MajorIdStatProvider.class::isInstance)
                .map(MajorIdStatProvider.class::cast)
                .findFirst()
                .map(provider -> new MajorIdFilterWidget(containerWidget, searchQuery, provider));
    }
}
