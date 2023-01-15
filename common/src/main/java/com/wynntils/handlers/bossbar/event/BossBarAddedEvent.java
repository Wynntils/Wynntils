/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.bossbar.event;

import com.wynntils.core.events.WynntilsEvent;
import com.wynntils.handlers.bossbar.TrackedBar;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class BossBarAddedEvent extends WynntilsEvent {
    private final TrackedBar trackedBar;

    public BossBarAddedEvent(TrackedBar trackedBar) {
        this.trackedBar = trackedBar;
    }

    public TrackedBar getTrackedBar() {
        return trackedBar;
    }
}
