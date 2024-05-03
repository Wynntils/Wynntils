/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.event;

import com.wynntils.models.raid.type.Raid;
import net.minecraftforge.eventbus.api.Event;

public abstract class RaidFinishedEvent extends Event {
    private final Raid raid;
    private final int raidTime;

    protected RaidFinishedEvent(Raid raid, int raidTime) {
        this.raid = raid;
        this.raidTime = raidTime;
    }

    public Raid getRaid() {
        return raid;
    }

    public int getRaidTime() {
        return raidTime;
    }

    public static class Completed extends RaidFinishedEvent {
        public Completed(Raid raid, int raidTime) {
            super(raid, raidTime);
        }
    }

    public static class Failed extends RaidFinishedEvent {
        public Failed(Raid raid, int raidTime) {
            super(raid, raidTime);
        }
    }
}
