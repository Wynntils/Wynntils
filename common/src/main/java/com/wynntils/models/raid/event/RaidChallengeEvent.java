/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.event;

import com.wynntils.models.raid.type.Raid;
import com.wynntils.models.raid.type.RaidRoomType;
import net.minecraftforge.eventbus.api.Event;

public abstract class RaidChallengeEvent extends Event {
    private final Raid raid;
    private final RaidRoomType challengeRoom;

    protected RaidChallengeEvent(Raid raid, RaidRoomType challengeRoom) {
        this.raid = raid;
        this.challengeRoom = challengeRoom;
    }

    public Raid getRaid() {
        return raid;
    }

    public RaidRoomType getChallengeRoom() {
        return challengeRoom;
    }

    public static class Started extends RaidChallengeEvent {
        public Started(Raid raid, RaidRoomType challengeRoom) {
            super(raid, challengeRoom);
        }
    }

    public static class Completed extends RaidChallengeEvent {
        public Completed(Raid raid, RaidRoomType challengeRoom) {
            super(raid, challengeRoom);
        }
    }
}
