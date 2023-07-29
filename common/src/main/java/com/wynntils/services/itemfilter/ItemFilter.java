/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter;

import com.wynntils.models.items.WynnItem;

public abstract class ItemFilter {
    protected String filterValue;

    protected ItemFilter(String filterValue) {
        this.filterValue = filterValue;
    }

    /**
     * Prepares the filter with the provided value. Similar to compiling for a regex
     * @throws InvalidSyntaxException if the provided value is not valid
     */
    public abstract void prepare() throws InvalidSyntaxException;

    /**
     * Matches the given item stack against the filter
     * @param wynnItem the item to match
     * @return true if the item stack matches the filter, false otherwise
     */
    public abstract boolean matches(WynnItem wynnItem);
}
