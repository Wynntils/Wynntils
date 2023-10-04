/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.wynntils.models.activities.type.ActivitySortOrder;
import java.util.List;

public interface SortableActivityScreen<T> {
    ActivitySortOrder getActivitySortOrder();

    void setActivitySortOrder(ActivitySortOrder newSortOrder);

    void activitiesChanged(List<T> activityInfos);
}
