/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.properties;

import com.wynntils.core.text.StyledText;

/**
 * Items that have multiple pages in the tooltip
 */
public interface PagedItemProperty {
    int currentPage();

    void setCurrentPage(int page);

    boolean hasParsedPage(int page);

    void markParsedPage(int page);

    StyledText getLore();

    void setLore(StyledText lore);

    default boolean isStatPage() {
        return currentPage() == 0;
    }

    default boolean isMiscStatsPage() {
        return currentPage() == 1;
    }

    default boolean isLorePage() {
        return currentPage() == 2;
    }
}
