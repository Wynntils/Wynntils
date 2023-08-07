/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.beacons.event;

import com.wynntils.models.beacons.type.Beacon;
import java.util.List;
import net.minecraft.world.entity.Entity;
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
        private final List<Entity> entities;

        public Added(Beacon verifiedBeacon, List<Entity> entities) {
            super(verifiedBeacon);
            this.entities = entities;
        }

        public List<Entity> getEntities() {
            return entities;
        }
    }

    public static class Moved extends BeaconEvent {
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

    public static class Removed extends BeaconEvent {
        public Removed(Beacon beacon) {
            super(beacon);
        }
    }
}
