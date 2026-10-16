/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.event;

import com.wynntils.models.lootrun.type.LootrunLocation;
import net.neoforged.bus.api.Event;

public class LootrunStartedEvent extends Event {
    private final LootrunLocation location;

    public LootrunStartedEvent(LootrunLocation location) {
        this.location = location;
    }

    public LootrunLocation getLocation() {
        return location;
    }
}
