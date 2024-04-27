/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets.numeric;

import com.wynntils.screens.itemfilter.ItemFilterScreen;
import com.wynntils.screens.itemfilter.widgets.ProviderFilterListWidget;
import com.wynntils.services.itemfilter.filters.RangedStatFilters;
import com.wynntils.services.itemfilter.type.StatFilter;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import java.util.Optional;

public class RangedIntegerFilterWidget extends RangedNumericFilterWidget<Integer> {
    public RangedIntegerFilterWidget(
            int x,
            int y,
            int width,
            int height,
            StatProviderAndFilterPair filterPair,
            ProviderFilterListWidget parent,
            ItemFilterScreen filterScreen) {
        super(x, y, width, height, parent, filterScreen);

        if (filterPair != null
                && filterPair.statFilter() instanceof RangedStatFilters.RangedIntegerStatFilter integerStatFilter) {
            setMinInput(String.valueOf(integerStatFilter.getMin()));
            setMaxInput(String.valueOf(integerStatFilter.getMax()));
        }
    }

    @Override
    protected Optional<StatFilter<Integer>> getRangedStatFilter(String min, String max) {
        return new RangedStatFilters.RangedIntegerStatFilter.RangedIntegerStatFilterFactory()
                .create(min + "-" + max)
                .map(f -> f);
    }
}
