/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.models.activities.type.ActivityType;

public class ActivityUpdatedEvent extends BaseEvent {
    private final ActivityType activityType;

    public ActivityUpdatedEvent(ActivityType activityType) {
        this.activityType = activityType;
    }

    public ActivityType getActivityType() {
        return activityType;
    }
}
