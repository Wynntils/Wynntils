/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.beacons.event;

import com.wynntils.models.beacons.type.VerifiedBeacon;
import net.minecraftforge.eventbus.api.Event;

public abstract class BeaconEvent extends Event {
    protected final VerifiedBeacon beacon;

    protected BeaconEvent(VerifiedBeacon beacon) {
        this.beacon = beacon;
    }

    public VerifiedBeacon getBeacon() {
        return beacon;
    }

    public static class Added extends BeaconEvent {
        public Added(VerifiedBeacon beacon) {
            super(beacon);
        }
    }

    public static class Moved extends BeaconEvent {
        public Moved(VerifiedBeacon beacon) {
            super(beacon);
        }
    }

    public static class Removed extends BeaconEvent {
        public Removed(VerifiedBeacon beacon) {
            super(beacon);
        }
    }
}
