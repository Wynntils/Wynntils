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
import java.util.function.Function;

public class StatProviderFilterMap {
    private final Map<ItemStatProvider<?>, List<StatProviderAndFilterPair>> map = new HashMap<>();

    public void put(ItemStatProvider<?> statProvider, StatFilter<?> statFilter) {
        map.computeIfAbsent(statProvider, k -> new ArrayList<>())
                .add(StatProviderAndFilterPair.fromPair(statProvider, statFilter));
    }

    public void put(StatProviderAndFilterPair filter) {
        map.computeIfAbsent(filter.statProvider(), k -> new ArrayList<>()).add(filter);
    }

    public void putAll(ItemStatProvider<?> statProvider, List<StatProviderAndFilterPair> filters) {
        map.computeIfAbsent(statProvider, k -> new ArrayList<>()).addAll(filters);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean matches(WynnItem wynnItem) {
        // We find a match if all the stat providers have at least one filter that matches the item.
        return map.entrySet().stream()
                .allMatch(entry -> entry.getValue().stream().anyMatch(pair -> pair.matches(wynnItem)));
    }

    public Map<ItemStatProvider<?>, List<StatProviderAndFilterPair>> entries() {
        return Map.copyOf(map);
    }

    public List<StatProviderAndFilterPair> values() {
        return map.values().stream().flatMap(List::stream).toList();
    }

    public List<StatProviderAndFilterPair> get(ItemStatProvider<?> selectedProvider) {
        return List.copyOf(map.getOrDefault(selectedProvider, List.of()));
    }

    public void removeIf(Function<StatProviderAndFilterPair, Boolean> filter) {
        map.values().forEach(list -> list.removeIf(filter::apply));

        // Remove any empty lists.
        map.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public boolean containsKey(ItemStatProvider<?> provider) {
        return map.containsKey(provider);
    }
}
