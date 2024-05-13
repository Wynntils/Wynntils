/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.type;

import com.wynntils.utils.type.Pair;
import java.util.List;
import net.minecraft.ChatFormatting;

public record ItemSearchQuery(
        String queryString,
        StatProviderFilterMap filters,
        List<SortInfo> sorts,
        List<Pair<ChatFormatting, Pair<Integer, Integer>>> colorRanges,
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
