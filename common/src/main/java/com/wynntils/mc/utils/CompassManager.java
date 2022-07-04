/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.utils;

import com.wynntils.core.WynntilsMod;
import com.wynntils.mc.event.SetSpawnEvent;
import com.wynntils.utils.objects.Location;
import net.minecraft.core.BlockPos;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// TODO: Draw compass beam
public class CompassManager {

    private static Location compassLocation = null;

    public static void init() {
        WynntilsMod.getEventBus().register(CompassManager.class);
    }

    public static Location getCompassLocation() {
        if (compassLocation != null) compassLocation.y = McUtils.player().getY();
        return compassLocation;
    }

    public static void setCompassLocation(Location compassLocation) {
        CompassManager.compassLocation = compassLocation;

        if (McUtils.level() != null) McUtils.level().setDefaultSpawnPos(compassLocation.toBlockPos(), 0);
    }

    public static void reset() {
        compassLocation = null;

        if (McUtils.level() != null) McUtils.level().setDefaultSpawnPos(null, 0);
    }

    @SubscribeEvent
    public static void onSetSpawn(SetSpawnEvent e) {
        BlockPos spawnPos = e.getSpawnPos();

        if (McUtils.player() == null) {
            // Reset compass
            CompassManager.reset();

            if (McUtils.level() != null) McUtils.level().setDefaultSpawnPos(spawnPos, 0);

            return;
        }

        // Cancel the event to force the compass to not change
        if (CompassManager.getCompassLocation() != null) {
            e.setCanceled(true);
        }
    }
}
