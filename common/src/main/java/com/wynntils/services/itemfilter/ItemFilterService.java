/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter;

import com.wynntils.core.components.Service;
import com.wynntils.utils.type.ErrorOr;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.resources.language.I18n;

public class ItemFilterService extends Service {
    private final Map<String, ItemFilterFactory> filterFactories = new HashMap<>();

    public ItemFilterService() {
        super(List.of());
        registerFilter(new LevelItemFilterFactory());
        registerFilter(new ProfessionItemFilterFactory());
    }

    private void registerFilter(ItemFilterFactory factory) {
        filterFactories.put(factory.getKeyword(), factory);
    }

    /**
     * Returns a filter factory for the given keyword, or an error string if the keyword does not match any filter.
     * @param keyword the keyword to get the filter factory for
     * @return the filter factory, or an error string if the keyword does not match any filter
     */
    public ErrorOr<? extends ItemFilterFactory> getFilterFactory(String keyword) {
        if (!filterFactories.containsKey(keyword)) {
            return ErrorOr.error(I18n.get("feature.wynntils.itemFilter.unknownFilter", keyword));
        }

        return ErrorOr.of(filterFactories.get(keyword));
    }

    /**
     * @return an unmodifiable collection of all registered filter factories
     */
    public Collection<ItemFilterFactory> getFilterFactories() {
        return Collections.unmodifiableCollection(filterFactories.values());
    }
}
