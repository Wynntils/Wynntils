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
import com.wynntils.utils.type.CappedValue;
import java.util.Optional;

public class RangedCappedValueFilterWidget extends RangedNumericFilterWidget<CappedValue> {
    public RangedCappedValueFilterWidget(
            int x,
            int y,
            int width,
            int height,
            StatProviderAndFilterPair filterPair,
            ProviderFilterListWidget parent,
            ItemFilterScreen filterScreen) {
        super(x, y, width, height, parent, filterScreen);

        if (filterPair != null
                && filterPair.statFilter()
                        instanceof RangedStatFilters.RangedCappedValueStatFilter cappedValueStatFilter) {
            setMinInput(String.valueOf(cappedValueStatFilter.getMin()));
            setMaxInput(String.valueOf(cappedValueStatFilter.getMax()));
        }
    }

    @Override
    protected Optional<StatFilter<CappedValue>> getRangedStatFilter(String min, String max) {
        return new RangedStatFilters.RangedCappedValueStatFilter.RangedCappedValueStatFilterFactory()
                .create(min + "-" + max)
                .map(f -> f);
    }
}
