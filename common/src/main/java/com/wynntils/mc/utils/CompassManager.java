/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.utils;

import com.wynntils.utils.objects.Location;

// TODO: Draw compass beam
public class CompassManager {

    private static Location compassLocation = null;

    public static Location getCompassLocation() {
        if (compassLocation != null) compassLocation.y = McUtils.player().getY();
        return compassLocation;
    }

    public static void setCompassLocation(Location compassLocation) {
        CompassManager.compassLocation = compassLocation;

        if (McUtils.mc().level != null) McUtils.mc().level.setDefaultSpawnPos(compassLocation.toBlockPos(), 0);
    }

    public static void reset() {
        compassLocation = null;

        if (McUtils.mc().level != null) McUtils.mc().level.setDefaultSpawnPos(null, 0);
    }
}
