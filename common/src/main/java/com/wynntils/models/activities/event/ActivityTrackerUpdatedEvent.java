/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.activities.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.activities.type.ActivityType;

public final class ActivityTrackerUpdatedEvent extends BaseEvent {
    private final ActivityType type;
    private final String name;
    private final StyledText task;

    public ActivityTrackerUpdatedEvent(ActivityType type, String name, StyledText task) {
        this.type = type;
        this.name = name;
        this.task = task;
    }

    public ActivityType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public StyledText getTask() {
        return task;
    }
}
