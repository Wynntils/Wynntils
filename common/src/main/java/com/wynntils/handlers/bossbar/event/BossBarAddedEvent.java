/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.bossbar.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.CancelRequestable;
import com.wynntils.handlers.bossbar.TrackedBar;

public class BossBarAddedEvent extends BaseEvent implements CancelRequestable {
    private final TrackedBar trackedBar;

    public BossBarAddedEvent(TrackedBar trackedBar) {
        this.trackedBar = trackedBar;
    }

    public TrackedBar getTrackedBar() {
        return trackedBar;
    }
}
