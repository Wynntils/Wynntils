/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter;

import com.wynntils.models.items.WynnItem;

public interface ItemFilter {
    /**
     * Matches the given item stack against the filter
     *
     * @param wynnItem the item to match
     * @return true if the item stack matches the filter, false otherwise
     */
    boolean matches(WynnItem wynnItem);
}
