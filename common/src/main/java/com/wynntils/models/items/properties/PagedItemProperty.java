/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.properties;

/**
 * Items that have multiple pages in the tooltip
 */
public interface PagedItemProperty {
    int currentPage();

    // This should only be updated from ItemHandler for now
    void updatePage(int newPage);

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
