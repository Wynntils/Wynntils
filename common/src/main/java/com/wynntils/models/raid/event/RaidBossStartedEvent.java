/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.raid.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.models.raid.raids.RaidKind;

public final class RaidBossStartedEvent extends BaseEvent {
    private final RaidKind raidKind;

    public RaidBossStartedEvent(RaidKind raidKind) {
        this.raidKind = raidKind;
    }

    public RaidKind getRaid() {
        return raidKind;
    }
}
