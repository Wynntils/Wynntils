/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.beacons.type;

import com.wynntils.utils.mc.type.PreciseLocation;
import java.util.Objects;

public final class Beacon<T extends BeaconKind> {
    private final PreciseLocation position;
    private final T beaconKind;

    public Beacon(PreciseLocation position, T beaconKind) {
        this.position = position;
        this.beaconKind = beaconKind;
    }

    public PreciseLocation position() {
        return position;
    }

    public T beaconKind() {
        return beaconKind;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Beacon) obj;
        return Objects.equals(this.position, that.position) && Objects.equals(this.beaconKind, that.beaconKind);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, beaconKind);
    }

    @Override
    public String toString() {
        return "Beacon[" + "position=" + position + ", " + "beaconKind=" + beaconKind + ']';
    }
}
