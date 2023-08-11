/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.event;

import com.wynntils.models.beacons.type.Beacon;
import net.minecraftforge.eventbus.api.Event;

public class LootrunBeaconSelectedEvent extends Event {
    private final Beacon beacon;

    public LootrunBeaconSelectedEvent(Beacon beacon) {
        this.beacon = beacon;
    }

    public Beacon getBeacon() {
        return beacon;
    }
}
