/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

import com.wynntils.screens.itemfilter.ItemFilterScreen;
import com.wynntils.services.itemfilter.filters.AnyStatFilters;
import com.wynntils.services.itemfilter.filters.RangedStatFilters;
import com.wynntils.services.itemfilter.type.ItemStatProvider;
import com.wynntils.services.itemfilter.type.StatFilter;
import com.wynntils.services.itemfilter.type.StatProviderAndFilterPair;
import com.wynntils.utils.type.CappedValue;
import java.util.List;
import java.util.Optional;

public class CappedValueWidget extends NumericValueWidget<CappedValue> {
    public CappedValueWidget(
            List<StatProviderAndFilterPair> filters,
            ItemStatProvider<?> itemStatProvider,
            ItemFilterScreen filterScreen) {
        super(filters, itemStatProvider, filterScreen);
    }

    @Override
    protected StatFilter<CappedValue> getAnyStatFilter() {
        return new AnyStatFilters.AnyCappedValueStatFilter.AnyCappedValueStatFilterFactory().create();
    }

    @Override
    protected Optional<StatFilter<CappedValue>> getSingleStatFilter(String value) {
        return new RangedStatFilters.RangedCappedValueStatFilter.RangedCappedValueStatFilterFactory()
                .create(value)
                .map(f -> f);
    }

    @Override
    protected Optional<StatFilter<CappedValue>> getRangedStatFilter(String min, String max) {
        return new RangedStatFilters.RangedCappedValueStatFilter.RangedCappedValueStatFilterFactory()
                .create(min + "-" + max)
                .map(f -> f);
    }

    @Override
    protected Optional<StatFilter<CappedValue>> getGreaterThanStatFilter(String value, boolean equal) {
        return new RangedStatFilters.RangedCappedValueStatFilter.RangedCappedValueStatFilterFactory()
                .create((equal ? ">=" : ">") + value)
                .map(f -> f);
    }

    @Override
    protected Optional<StatFilter<CappedValue>> getLessThanStatFilter(String value, boolean equal) {
        return new RangedStatFilters.RangedCappedValueStatFilter.RangedCappedValueStatFilterFactory()
                .create((equal ? "<=" : "<") + value)
                .map(f -> f);
    }
}
