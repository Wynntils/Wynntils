/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.type;

import com.wynntils.models.items.WynnItem;
import java.util.List;

public record StatProviderAndFilterPair<T>(ItemStatProvider statProvider, StatFilter<T> statFilter) {
    public static StatProviderAndFilterPair<Object> fromPair(ItemStatProvider itemStatProvider, StatFilter value) {
        return new StatProviderAndFilterPair<>(itemStatProvider, value);
    }

    public boolean matches(WynnItem wynnItem) {
        List<T> statProviderValues = statProvider.getValue(wynnItem);
        return statFilter.matches(statProviderValues);
    }
}
