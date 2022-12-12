/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.map.poi;

import com.wynntils.mc.objects.Location;
import java.util.Objects;
import java.util.Optional;

public class PoiLocation {
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
        return new Location(x, y, z);
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + "]";
    }

    public String asChatCoordinates() {
        return x + " " + y + " " + z;
    }

    public static PoiLocation fromLocation(Location location) {
        if (location == null) return null;

        return new PoiLocation((int) location.x, (int) location.y, (int) location.z);
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
