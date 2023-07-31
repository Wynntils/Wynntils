/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.event;

import com.wynntils.models.beacons.type.VerifiedBeacon;
import net.minecraftforge.eventbus.api.Event;

public class LootrunBeaconSelectedEvent extends Event {
    private final VerifiedBeacon beacon;

    public LootrunBeaconSelectedEvent(VerifiedBeacon beacon) {
        this.beacon = beacon;
    }

    public VerifiedBeacon getBeacon() {
        return beacon;
    }
}
