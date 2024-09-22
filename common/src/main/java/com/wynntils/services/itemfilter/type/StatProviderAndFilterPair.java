/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.type;

import com.wynntils.models.items.WynnItem;
import java.util.Optional;

public record StatProviderAndFilterPair<T>(ItemStatProvider statProvider, StatFilter<T> statFilter) {
    public static StatProviderAndFilterPair fromPair(ItemStatProvider itemStatProvider, StatFilter value) {
        return new StatProviderAndFilterPair(itemStatProvider, value);
    }

    public boolean matches(WynnItem wynnItem) {
        Optional<T> statProviderValue = statProvider.getValue(wynnItem);
        return statProviderValue.isPresent() && statFilter.matches(statProviderValue.get());
    }
}
