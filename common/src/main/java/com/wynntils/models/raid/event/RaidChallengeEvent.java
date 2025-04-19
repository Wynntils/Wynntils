/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.event;

import com.wynntils.models.raid.type.RaidInfo;
import net.neoforged.bus.api.Event;

public abstract class RaidChallengeEvent extends Event {
    private final RaidInfo raidInfo;

    protected RaidChallengeEvent(RaidInfo raidInfo) {
        this.raidInfo = raidInfo;
    }

    public RaidInfo getRaid() {
        return raidInfo;
    }

    public static class Started extends RaidChallengeEvent {
        public Started(RaidInfo raidInfo) {
            super(raidInfo);
        }
    }

    public static class Completed extends RaidChallengeEvent {
        public Completed(RaidInfo raidInfo) {
            super(raidInfo);
        }
    }
}
