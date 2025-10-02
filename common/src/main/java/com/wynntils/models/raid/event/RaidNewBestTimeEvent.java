/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.event;

import com.wynntils.core.events.BaseEvent;

public class RaidNewBestTimeEvent extends BaseEvent {
    private final String raidName;
    private final long time;

    public RaidNewBestTimeEvent(String raidName, long time) {
        this.raidName = raidName;
        this.time = time;
    }

    public String getRaidName() {
        return raidName;
    }

    public long getTime() {
        return time;
    }
}
