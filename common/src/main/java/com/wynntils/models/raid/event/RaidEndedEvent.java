/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.models.raid.type.RaidInfo;

public abstract class RaidEndedEvent extends BaseEvent {
    private final RaidInfo raidInfo;

    protected RaidEndedEvent(RaidInfo raidInfo) {
        this.raidInfo = raidInfo;
    }

    public RaidInfo getRaid() {
        return raidInfo;
    }

    public static class Completed extends RaidEndedEvent {
        public Completed(RaidInfo raidInfo) {
            super(raidInfo);
        }
    }

    public static class Failed extends RaidEndedEvent {
        public Failed(RaidInfo raidInfo) {
            super(raidInfo);
        }
    }
}
