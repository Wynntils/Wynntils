/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.event;

import com.wynntils.models.raid.raids.RaidKind;
import net.neoforged.bus.api.Event;

public class RaidBossStartedEvent extends Event {
    private final RaidKind raidKind;

    public RaidBossStartedEvent(RaidKind raidKind) {
        this.raidKind = raidKind;
    }

    public RaidKind getRaid() {
        return raidKind;
    }
}
