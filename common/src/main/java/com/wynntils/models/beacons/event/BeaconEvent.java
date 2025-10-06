/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.beacons.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.models.beacons.type.Beacon;
import net.minecraft.world.entity.Entity;

public abstract class BeaconEvent extends BaseEvent {
    protected final Beacon beacon;

    protected BeaconEvent(Beacon beacon) {
        this.beacon = beacon;
    }

    public Beacon getBeacon() {
        return beacon;
    }

    public static final class Added extends BeaconEvent {
        private final Entity entity;

        public Added(Beacon verifiedBeacon, Entity entities) {
            super(verifiedBeacon);
            this.entity = entities;
        }

        public Entity getEntity() {
            return entity;
        }
    }

    public static final class Moved extends BeaconEvent {
        private final Beacon newBeacon;

        public Moved(Beacon oldBeacon, Beacon newBeacon) {
            super(oldBeacon);
            this.newBeacon = newBeacon;
        }

        @Override
        public Beacon getBeacon() {
            throw new UnsupportedOperationException("Use getOldBeacon() or getNewBeacon() instead");
        }

        public Beacon getOldBeacon() {
            return beacon;
        }

        public Beacon getNewBeacon() {
            return newBeacon;
        }
    }

    public static final class Removed extends BeaconEvent {
        public Removed(Beacon beacon) {
            super(beacon);
        }
    }
}
