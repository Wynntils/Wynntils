/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.event;

import com.wynntils.models.raid.type.RaidKind;
import java.util.Collections;
import java.util.List;
import net.neoforged.bus.api.Event;

public abstract class RaidEndedEvent extends Event {
    private final RaidKind raidKind;
    private final List<Long> roomTimes;
    private final long raidTime;

    protected RaidEndedEvent(RaidKind raidKind, List<Long> roomTimes, long raidTime) {
        this.raidKind = raidKind;
        this.roomTimes = roomTimes;
        this.raidTime = raidTime;
    }

    public RaidKind getRaid() {
        return raidKind;
    }

    public List<Long> getRoomTimes() {
        return roomTimes;
    }

    public long getRaidTime() {
        return raidTime;
    }

    public static class Completed extends RaidEndedEvent {
        private final List<Long> roomDamages;
        private final Long raidDamage;

        public Completed(
                RaidKind raidKind, List<Long> roomTimes, long raidTime, List<Long> roomDamages, Long raidDamage) {
            super(raidKind, roomTimes, raidTime);

            this.roomDamages = roomDamages;
            this.raidDamage = raidDamage;
        }

        public List<Long> getRoomDamages() {
            return Collections.unmodifiableList(roomDamages);
        }

        public Long getRaidDamage() {
            return raidDamage;
        }
    }

    public static class Failed extends RaidEndedEvent {
        public Failed(RaidKind raidKind, List<Long> roomTimes, long raidTime) {
            super(raidKind, roomTimes, raidTime);
        }
    }
}
