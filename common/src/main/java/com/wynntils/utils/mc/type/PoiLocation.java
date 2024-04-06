/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc.type;

import java.util.Objects;
import java.util.Optional;

public class PoiLocation {
    private static final int DEFAULT_Y = 64;

    private final int x;
    private final Integer y;
    private final int z;

    public PoiLocation(int x, Integer y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public Optional<Integer> getY() {
        return Optional.ofNullable(y);
    }

    public int getZ() {
        return z;
    }

    public Location asLocation() {
        return new Location(x, y == null ? DEFAULT_Y : y, z);
    }

    @Override
    public String toString() {
        // Use short form if we're missing y coordinate
        if (y == null) return "[" + x + ", " + z + "]";

        return "[" + x + ", " + y + ", " + z + "]";
    }

    public String asChatCoordinates() {
        return x + " " + y + " " + z;
    }

    public static PoiLocation fromLocation(Location location) {
        if (location == null) return null;

        return new PoiLocation(location.x, location.y, location.z);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        PoiLocation that = (PoiLocation) other;
        return x == that.x && Objects.equals(y, that.y) && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
