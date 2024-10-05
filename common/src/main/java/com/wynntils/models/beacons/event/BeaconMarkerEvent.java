/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.beacons.event;

import com.wynntils.models.beacons.type.BeaconMarker;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;

public abstract class BeaconMarkerEvent extends Event {
    protected final BeaconMarker beaconMarker;

    protected BeaconMarkerEvent(BeaconMarker beaconMarker) {
        this.beaconMarker = beaconMarker;
    }

    public BeaconMarker getBeaconMarker() {
        return beaconMarker;
    }

    public static class Added extends BeaconMarkerEvent {
        private final Entity entity;

        public Added(BeaconMarker verifiedBeaconMarker, Entity entities) {
            super(verifiedBeaconMarker);
            this.entity = entities;
        }

        public Entity getEntity() {
            return entity;
        }
    }

    public static class Moved extends BeaconMarkerEvent {
        private final BeaconMarker newMarker;

        public Moved(BeaconMarker oldMarker, BeaconMarker newMarker) {
            super(oldMarker);
            this.newMarker = newMarker;
        }

        @Override
        public BeaconMarker getBeaconMarker() {
            throw new UnsupportedOperationException("Use getOldBeaconMarker() or getNewBeaconMarker() instead");
        }

        public BeaconMarker getOldBeaconMarker() {
            return beaconMarker;
        }

        public BeaconMarker getNewBeaconMarker() {
            return newMarker;
        }
    }

    public static class Removed extends BeaconMarkerEvent {
        public Removed(BeaconMarker beaconMarker) {
            super(beaconMarker);
        }
    }
}
