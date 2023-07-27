/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory.search;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.resources.language.I18n;

public class WynnItemFilterManager implements WynnItemFilterFactory {
    private final Map<String, Function<String, ? extends WynnItemFilter>> filterSuppliers;

    private final Map<String, String> filterUsages;

    public WynnItemFilterManager() {
        filterUsages = new HashMap<>();
        filterSuppliers = new HashMap<>();
        registerFilter("lvl", "level", LevelSearchFilter::new);
        registerFilter("prof", "profession", ProfessionSearchFilter::new);
    }

    private void registerFilter(
            String keyword, String translateKey, Function<String, ? extends WynnItemFilter> supplier) {
        filterSuppliers.put(keyword, supplier);
        filterUsages.put(keyword, I18n.get("feature.wynntils.containerSearch.filter." + translateKey + ".usage"));
    }

    @Override
    public WynnItemFilter create(String keyword, String searchString) throws UnknownFilterException {
        if (!filterSuppliers.containsKey(keyword)) throw new UnknownFilterException(keyword);

        return filterSuppliers.get(keyword).apply(searchString);
    }

    public Map<String, String> getFilterUsages() {
        return Collections.unmodifiableMap(filterUsages);
    }
}
