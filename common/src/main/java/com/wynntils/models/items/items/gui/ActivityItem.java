/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.items.gui;

import com.wynntils.models.activities.type.ActivityInfo;

public class ActivityItem extends GuiItem {
    private final ActivityInfo activityInfo;

    public ActivityItem(ActivityInfo activityInfo) {
        this.activityInfo = activityInfo;
    }

    public ActivityInfo getActivityInfo() {
        return activityInfo;
    }

    @Override
    public String toString() {
        return "ActivityItem{" + "activityInfo=" + activityInfo + '}';
    }
}
