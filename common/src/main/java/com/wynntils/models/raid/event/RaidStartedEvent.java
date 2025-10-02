/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.models.raid.raids.RaidKind;

public class RaidStartedEvent extends BaseEvent {
    private final RaidKind raidKind;

    public RaidStartedEvent(RaidKind raidKind) {
        this.raidKind = raidKind;
    }

    public RaidKind getRaidKind() {
        return raidKind;
    }
}
