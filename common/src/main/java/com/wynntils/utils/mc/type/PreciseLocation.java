/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc.type;

import java.util.Comparator;
import java.util.Objects;
import net.minecraft.core.Position;
import net.minecraft.world.phys.Vec3;

public class PreciseLocation implements Position, Comparable<PreciseLocation> {
    // Compare first x, then z, and finally y
    private static final Comparator<PreciseLocation> LOCATION_COMPARATOR = Comparator.comparing(
                    PreciseLocation::x, Double::compareTo)
            .thenComparing(PreciseLocation::z, Double::compareTo)
            .thenComparing(PreciseLocation::y, Double::compareTo);

    private final double x;
    private final double y;
    private final double z;

    public PreciseLocation(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static PreciseLocation fromVec(Vec3 vec3) {
        return new PreciseLocation(vec3.x, vec3.y, vec3.z);
    }

    @Override
    public double x() {
        return x;
    }

    @Override
    public double y() {
        return y;
    }

    @Override
    public double z() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PreciseLocation that = (PreciseLocation) o;
        return Double.compare(x, that.x) == 0 && Double.compare(y, that.y) == 0 && Double.compare(z, that.z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    public String toString() {
        return "[" + this.x + ", " + this.y + ", " + this.z + "]";
    }

    @Override
    public int compareTo(PreciseLocation preciseLocation) {
        return LOCATION_COMPARATOR.compare(this, preciseLocation);
    }
}
