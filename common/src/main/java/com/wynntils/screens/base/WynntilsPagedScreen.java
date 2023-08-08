/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base;

public interface WynntilsPagedScreen {
    int getCurrentPage();

    void setCurrentPage(int currentPage);

    int getMaxPage();
}
