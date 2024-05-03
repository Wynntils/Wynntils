/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.event;

import com.wynntils.models.raid.type.Raid;
import net.minecraftforge.eventbus.api.Event;

public class RaidNewBestTimeEvent extends Event {
    private final Raid raid;
    private final int time;

    public RaidNewBestTimeEvent(Raid raid, int time) {
        this.raid = raid;
        this.time = time;
    }

    public Raid getRaid() {
        return raid;
    }

    public int getTime() {
        return time;
    }
}
