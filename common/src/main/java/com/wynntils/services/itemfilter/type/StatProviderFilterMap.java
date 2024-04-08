/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.type;

import com.wynntils.models.items.WynnItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatProviderFilterMap {
    private final Map<ItemStatProvider<?>, List<StatProviderAndFilterPair>> map = new HashMap<>();

    public void put(ItemStatProvider<?> statProvider, StatFilter<?> statFilter) {
        map.computeIfAbsent(statProvider, k -> new ArrayList<>())
                .add(StatProviderAndFilterPair.fromPair(statProvider, statFilter));
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean matches(WynnItem wynnItem) {
        // We find a match if all the stat providers have at least one filter that matches the item.
        return map.entrySet().stream()
                .allMatch(entry -> entry.getValue().stream().anyMatch(pair -> pair.matches(wynnItem)));
    }

    public List<StatProviderAndFilterPair> values() {
        return map.values().stream().flatMap(List::stream).toList();
    }
}
