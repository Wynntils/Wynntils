/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.event;

import com.wynntils.models.raid.raids.RaidKind;
import net.neoforged.bus.api.Event;

public class RaidStartedEvent extends Event {
    private final RaidKind raidKind;

    public RaidStartedEvent(RaidKind raidKind) {
        this.raidKind = raidKind;
    }

    public RaidKind getRaidKind() {
        return raidKind;
    }
}
