/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.type;

import com.wynntils.models.items.WynnItem;

/**
 * An {@link ItemFilterMatcher} is created by an {@link ItemFilter} and is used to hold the logic
 * of a filter, instantiated with a specific input string.
 */
@FunctionalInterface
public interface ItemFilterMatcher {
    /**
     * Returns true if the item stack matches the filter, false otherwise.
     *
     * @param wynnItem the item to match
     * @return true if the item stack matches the filter, false otherwise
     */
    boolean matches(WynnItem wynnItem);
}
