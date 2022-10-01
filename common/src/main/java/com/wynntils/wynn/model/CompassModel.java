/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.wynntils.core.managers.Model;
import com.wynntils.mc.event.SetSpawnEvent;
import com.wynntils.mc.objects.Location;
import com.wynntils.mc.utils.McUtils;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class CompassModel extends Model {
    private static Location compassLocation = null;

    public static void init() {}

    public static Optional<Location> getCompassLocation() {
        if (compassLocation != null) compassLocation.y = McUtils.player().getY();

        return Optional.ofNullable(compassLocation);
    }

    public static void setCompassLocation(Location compassLocation) {
        CompassModel.compassLocation = compassLocation;

        if (McUtils.mc().level != null) {
            McUtils.mc().level.setDefaultSpawnPos(compassLocation.toBlockPos(), 0);
        }
    }

    public static void reset() {
        compassLocation = null;

        if (McUtils.mc().level != null) {
            // We can't remove the compass behavior, so arbitrarily set it to our
            // current position
            McUtils.mc().level.setDefaultSpawnPos(McUtils.player().blockPosition(), 0);
        }
    }

    @SubscribeEvent
    public static void onSetSpawn(SetSpawnEvent e) {
        BlockPos spawnPos = e.getSpawnPos();

        if (McUtils.player() == null) {
            // Reset compass
            compassLocation = null;

            if (McUtils.mc().level != null) {
                McUtils.mc().level.setDefaultSpawnPos(spawnPos, 0);
            }

            return;
        }

        if (compassLocation != null) {
            // If we have a set location, do not update our spawn point
            e.setCanceled(true);
        }
    }
}
