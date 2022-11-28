/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

import com.wynntils.mc.objects.Location;
import java.util.Objects;

public class MapLocation {
    private final int x;
    private final int y;
    private final int z;

    public MapLocation(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Location asLocation() {
        return new Location(x, y, z);
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + "]";
    }

    public String asChatCoordinates() {
        return x + " " + y + " " + z;
    }

    public static MapLocation fromLocation(Location location) {
        if (location == null) return null;

        return new MapLocation((int) location.x, (int) location.y, (int) location.z);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        MapLocation that = (MapLocation) other;
        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
