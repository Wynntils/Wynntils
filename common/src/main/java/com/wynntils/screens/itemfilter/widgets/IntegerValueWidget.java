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
import java.util.Optional;

public class IntegerValueWidget extends NumericValueWidget<Integer> {
    public IntegerValueWidget(ItemStatProvider<?> itemStatProvider, ItemFilterScreen filterScreen) {
        super(itemStatProvider, filterScreen);
    }

    @Override
    protected StatFilter<Integer> getAnyStatFilter() {
        return new AnyStatFilters.AnyIntegerStatFilter.AnyIntegerStatFilterFactory().create();
    }

    @Override
    protected Optional<StatFilter<Integer>> getSingleStatFilter(String value) {
        return new RangedStatFilters.RangedIntegerStatFilter.RangedIntegerStatFilterFactory()
                .create(value)
                .map(f -> f);
    }

    @Override
    protected Optional<StatFilter<Integer>> getRangedStatFilter(String min, String max) {
        return new RangedStatFilters.RangedIntegerStatFilter.RangedIntegerStatFilterFactory()
                .create(min + "-" + max)
                .map(f -> f);
    }

    @Override
    protected Optional<StatFilter<Integer>> getGreaterThanStatFilter(String value, boolean equal) {
        return new RangedStatFilters.RangedIntegerStatFilter.RangedIntegerStatFilterFactory()
                .create((equal ? ">=" : ">") + value)
                .map(f -> f);
    }

    @Override
    protected Optional<StatFilter<Integer>> getLessThanStatFilter(String value, boolean equal) {
        return new RangedStatFilters.RangedIntegerStatFilter.RangedIntegerStatFilterFactory()
                .create((equal ? "<=" : "<") + value)
                .map(f -> f);
    }
}
