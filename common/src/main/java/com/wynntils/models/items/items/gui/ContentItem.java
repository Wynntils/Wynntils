/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.activities.type.ActivityInfo;

public class ContentItem extends GuiItem {
    private final ActivityInfo activityInfo;

    public ContentItem(ActivityInfo activityInfo) {
        this.activityInfo = activityInfo;
    }

    public ActivityInfo getContentInfo() {
        return activityInfo;
    }

    @Override
    public String toString() {
        return "ContentItem{" + "contentInfo=" + activityInfo + '}';
    }
}
