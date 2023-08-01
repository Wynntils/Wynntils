/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.beacons.event;

import com.wynntils.models.beacons.type.Beacon;
import net.minecraftforge.eventbus.api.Event;

public abstract class BeaconEvent extends Event {
    protected final Beacon beacon;

    protected BeaconEvent(Beacon beacon) {
        this.beacon = beacon;
    }

    public Beacon getBeacon() {
        return beacon;
    }

    public static class Added extends BeaconEvent {
        public Added(Beacon beacon) {
            super(beacon);
        }
    }

    public static class Moved extends BeaconEvent {
        public Moved(Beacon oldBeacon, Beacon newBeacon) {
            super(oldBeacon);
        }
    }

    public static class Removed extends BeaconEvent {
        public Removed(Beacon beacon) {
            super(beacon);
        }
    }
}
