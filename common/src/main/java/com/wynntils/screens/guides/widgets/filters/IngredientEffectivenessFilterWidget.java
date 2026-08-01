/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets.filters;

import com.wynntils.core.components.Services;
import com.wynntils.models.ingredients.type.IngredientPosition;
import com.wynntils.screens.guides.widgets.GuideContainerWidget;
import com.wynntils.services.itemfilter.statproviders.IngredientEffectivenessStatProvider;
import com.wynntils.services.itemfilter.type.ItemSearchQuery;
import com.wynntils.services.itemfilter.type.ItemStatProvider;

public class IngredientEffectivenessFilterWidget extends IntegerStatFilterWidget<IngredientEffectivenessStatProvider> {
    private final IngredientPosition position;

    public IngredientEffectivenessFilterWidget(
            IngredientPosition position, GuideContainerWidget<?> containerWidget, ItemSearchQuery searchQuery) {
        super(containerWidget, searchQuery, -250, 250);

        this.position = position;

        getProvider();
        rebuildWidgets(searchQuery);
    }

    @Override
    public ItemStatProvider<?> getProvider() {
        provider = Services.ItemFilter.getItemStatProviders().stream()
                .filter(statProvider -> statProvider instanceof IngredientEffectivenessStatProvider provider
                        && provider.getIngredientPosition() == position)
                .map(statProvider -> (IngredientEffectivenessStatProvider) statProvider)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not get ingredient effectiveness stat provider"));
        return provider;
    }
}
