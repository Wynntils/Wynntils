/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.inventory.search;

public interface WynnItemFilterFactory {
    /**
     * Creates a new WynnItemFilter based on the given keyword an initates it with the given search string
     *
     * @param keyword the keyword associated with the filter
     * @param searchString the search string to use
     * @return the created WynnItemFilter
     * @throws UnknownFilterException if the keyword is not associated with a filter
     */
    WynnItemFilter create(String keyword, String searchString) throws UnknownFilterException;
}
