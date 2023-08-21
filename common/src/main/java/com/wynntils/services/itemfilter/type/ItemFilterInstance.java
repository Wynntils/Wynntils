/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.type;

import com.wynntils.models.items.WynnItem;

/**
 * An {@link ItemFilterInstance} is created by an {@link ItemFilter} and represents a filter instanciated with a
 * specific input string. It holds the logic that will check if an item matches the filter with the given input string.
 */
@FunctionalInterface
public interface ItemFilterInstance {
    /**
     * Returns true if the item stack matches the filter, false otherwise.
     *
     * @param wynnItem the item to match
     * @return true if the item stack matches the filter, false otherwise
     */
    boolean matches(WynnItem wynnItem);
}
