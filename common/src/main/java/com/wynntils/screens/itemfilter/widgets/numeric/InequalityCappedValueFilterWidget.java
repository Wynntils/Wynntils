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

public class InequalityCappedValueFilterWidget extends InequalityNumericFilterWidget<CappedValue> {
    public InequalityCappedValueFilterWidget(
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
            if (cappedValueStatFilter.getMin() != Integer.MIN_VALUE
                    && cappedValueStatFilter.getMax() == Integer.MAX_VALUE) {
                if (cappedValueStatFilter.isEqualsInString()) {
                    setInequalityType(InequalityType.GREATER_THAN_EQUAL);
                    setEntryInput(String.valueOf(cappedValueStatFilter.getMin()));
                } else {
                    // When equality is not enabled, we need to subtract 1 from the value,
                    // as getMin() returns the lowest included value
                    setInequalityType(InequalityType.GREATER_THAN);
                    setEntryInput((String.valueOf(cappedValueStatFilter.getMin() - 1)));
                }
            } else if (cappedValueStatFilter.getMax() != Integer.MAX_VALUE
                    && cappedValueStatFilter.getMin() == Integer.MIN_VALUE) {
                if (cappedValueStatFilter.isEqualsInString()) {
                    setInequalityType(InequalityType.LESS_THAN_EQUAL);
                    setEntryInput(String.valueOf(cappedValueStatFilter.getMax()));
                } else {
                    // When equality is not enabled, we need to add 1 to the value,
                    // as getMax() returns the highest included value
                    setInequalityType(InequalityType.LESS_THAN);
                    setEntryInput(String.valueOf(cappedValueStatFilter.getMax() + 1));
                }
            }
        }
    }

    @Override
    protected Optional<StatFilter<CappedValue>> getInequalityStatFilter(String value, InequalityType inequalityType) {
        return new RangedStatFilters.RangedCappedValueStatFilter.RangedCappedValueStatFilterFactory()
                .create(inequalityType.getMessage() + value)
                .map(f -> f);
    }
}
