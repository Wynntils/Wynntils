/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter;

import com.wynntils.models.items.WynnItem;

public abstract class ItemFilter {
    protected String searchString;

    protected ItemFilter(String searchString) {
        this.searchString = searchString;
    }

    /**
     * Prepares the operator with the given search string. Similar to compiling for a regex
     * @return true if the operator was prepared successfully
     * @throws InvalidSyntaxException if the search string is not invalid
     */
    public abstract boolean prepare() throws InvalidSyntaxException;

    /**
     * Matches the given item stack against the operator
     * @param wynnItem the item to match
     * @return true if the item stack matches the operator
     */
    public abstract boolean matches(WynnItem wynnItem);
}
