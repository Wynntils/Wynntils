/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.event;

import com.wynntils.models.raid.type.Raid;
import net.minecraftforge.eventbus.api.Event;

public class RaidBossStartedEvent extends Event {
    private final Raid raid;

    public RaidBossStartedEvent(Raid raid) {
        this.raid = raid;
    }

    public Raid getRaid() {
        return raid;
    }
}
