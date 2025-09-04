/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc.type;

import java.util.Comparator;
import net.minecraft.core.Position;
import net.minecraft.world.phys.Vec3;

public record PreciseLocation(double x, double y, double z) implements Position, Comparable<PreciseLocation> {
    // Compare first x, then z, and finally y
    private static final Comparator<PreciseLocation> LOCATION_COMPARATOR = Comparator.comparing(
                    PreciseLocation::x, Double::compareTo)
            .thenComparing(PreciseLocation::z, Double::compareTo)
            .thenComparing(PreciseLocation::y, Double::compareTo);

    public static PreciseLocation fromVec(Vec3 vec3) {
        return new PreciseLocation(vec3.x, vec3.y, vec3.z);
    }

    public String toString() {
        return "[" + this.x + ", " + this.y + ", " + this.z + "]";
    }

    @Override
    public int compareTo(PreciseLocation preciseLocation) {
        return LOCATION_COMPARATOR.compare(this, preciseLocation);
    }
}
