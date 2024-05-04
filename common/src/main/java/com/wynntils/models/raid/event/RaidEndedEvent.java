/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.event;

import com.wynntils.models.raid.type.RaidKind;
import net.minecraftforge.eventbus.api.Event;

public abstract class RaidEndedEvent extends Event {
    private final RaidKind raidKind;
    private final int raidTime;

    protected RaidEndedEvent(RaidKind raidKind, int raidTime) {
        this.raidKind = raidKind;
        this.raidTime = raidTime;
    }

    public RaidKind getRaid() {
        return raidKind;
    }

    public int getRaidTime() {
        return raidTime;
    }

    public static class Completed extends RaidEndedEvent {
        public Completed(RaidKind raidKind, int raidTime) {
            super(raidKind, raidTime);
        }
    }

    public static class Failed extends RaidEndedEvent {
        public Failed(RaidKind raidKind, int raidTime) {
            super(raidKind, raidTime);
        }
    }
}
