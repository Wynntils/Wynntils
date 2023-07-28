/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter;

import com.wynntils.core.components.Service;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ItemFilterService extends Service {
    private final Map<String, Function<String, ? extends ItemFilter>> filterSuppliers = new HashMap<>();

    private final Map<String, String> filterUsages = new HashMap<>();

    public ItemFilterService() {
        super(List.of());
        registerFilter("lvl", "level", LevelSearchFilter::new);
        registerFilter("prof", "profession", ProfessionSearchFilter::new);
    }

    private void registerFilter(String keyword, String translateKey, Function<String, ? extends ItemFilter> supplier) {
        filterSuppliers.put(keyword, supplier);
        filterUsages.put(keyword, "feature.wynntils.itemFilters." + translateKey + ".usage");
    }

    /**
     * Creates a new ItemFilter based on the given keyword an initates it with the given search string
     *
     * @param keyword the keyword associated with the filter
     * @param searchString the search string to use
     * @return the created ItemFilter
     * @throws UnknownFilterException if the keyword is not associated with a filter
     */
    public ItemFilter createFilter(String keyword, String searchString) throws UnknownFilterException {
        if (!filterSuppliers.containsKey(keyword)) {
            throw new UnknownFilterException(keyword);
        }

        return filterSuppliers.get(keyword).apply(searchString);
    }

    /**
     * Returns a mapping of all registered filter keywords to their usage translation keys
     * @return a mapping of all registered filter keywords to their usage translation keys
     */
    public Map<String, String> getFilterUsages() {
        return Collections.unmodifiableMap(filterUsages);
    }
}
