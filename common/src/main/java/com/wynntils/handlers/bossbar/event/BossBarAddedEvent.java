/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.bossbar.event;

import com.wynntils.handlers.bossbar.TrackedBar;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class BossBarAddedEvent extends Event {
    private final TrackedBar trackedBar;

    public BossBarAddedEvent(TrackedBar trackedBar) {
        this.trackedBar = trackedBar;
    }

    public TrackedBar getTrackedBar() {
        return trackedBar;
    }
}
