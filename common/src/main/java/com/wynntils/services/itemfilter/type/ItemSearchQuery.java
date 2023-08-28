/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.type;

import java.util.List;

public record ItemSearchQuery(
        String queryString,
        List<StatProviderAndFilterPair<?>> filters,
        List<Integer> ignoredCharIndices,
        List<Integer> validFilterCharIndices,
        List<String> errors,
        List<String> plainTextTokens) {
    /**
     * Checks if the query contains no valid filters or plain text tokens.
     *
     * @return true if the query contains no valid filters or plain text tokens.
     */
    public boolean isEmpty() {
        return filters.isEmpty() && plainTextTokens.isEmpty();
    }
}
