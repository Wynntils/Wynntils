/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.mc.type.Location;
import java.util.List;

@SuppressWarnings("unused") // Functions are accessed via reflection
public final class LocationFunctions {

    @TemplateFunction(name = "x", isPure = true)
    public static int xFunction(Location location) {
        return location.x();
    }

    @TemplateFunction(name = "y", isPure = true)
    public static int yFunction(Location location) {
        return location.y();
    }

    @TemplateFunction(name = "z", isPure = true)
    public static int zFunction(Location location) {
        return location.z();
    }

    @TemplateFunction(name = "location", aliases = "loc", isPure = true)
    public static Location locationFunction(int x, int y, int z) {
        return new Location(x, y, z);
    }

    @TemplateFunction(name = "distance", isPure = true)
    public static double distanceFunction(Location from, Location to) {
        return from.toVec3().distanceTo(to.toVec3());
    }

}
