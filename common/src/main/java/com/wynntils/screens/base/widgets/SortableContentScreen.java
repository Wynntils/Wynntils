/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.wynntils.models.content.type.ContentSortOrder;

public interface SortableContentScreen {
    ContentSortOrder getContentSortOrder();

    void setContentSortOrder(ContentSortOrder newSortOrder);
}
