/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.beacons.type;

import java.util.Objects;
import net.minecraft.world.phys.Vec3;

public final class Beacon<T extends BeaconKind> {
    private final Vec3 position;
    private final T beaconKind;

    public Beacon(Vec3 position, T beaconKind) {
        this.position = position;
        this.beaconKind = beaconKind;
    }

    public Vec3 position() {
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
