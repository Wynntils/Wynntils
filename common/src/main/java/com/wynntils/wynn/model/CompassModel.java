/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model;

import com.wynntils.core.components.Model;
import com.wynntils.gui.render.Texture;
import com.wynntils.mc.event.SetSpawnEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.objects.Location;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.wynn.model.map.poi.PoiLocation;
import com.wynntils.wynn.model.map.poi.WaypointPoi;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class CompassModel extends Model {
    private Supplier<Location> locationSupplier = null;
    private Location compassLocation = null; // this field acts as a cache for the supplier
    private Texture targetIcon = null;
    private CustomColor targetColor = null;

    @SubscribeEvent
    public void onTick(TickEvent e) {
        if (locationSupplier == null) return;

        Location newLocation = locationSupplier.get();

        if (newLocation == null) { // drop location
            reset();
        } else if (compassLocation != newLocation) { // update location
            compassLocation = newLocation;

            if (McUtils.mc().level != null) {
                McUtils.mc().level.setDefaultSpawnPos(compassLocation.toBlockPos(), 0);
            }
        }
    }

    public Optional<Location> getCompassLocation() {
        return Optional.ofNullable(compassLocation);
    }

    public Optional<WaypointPoi> getCompassWaypoint() {
        if (locationSupplier != null && locationSupplier.get() != null) {
            WaypointPoi waypointPoi = new WaypointPoi(() -> {
                Location location = locationSupplier.get();

                return PoiLocation.fromLocation(location);
            });

            return Optional.of(waypointPoi);
        }

        return Optional.empty();
    }

    public Texture getTargetIcon() {
        return targetIcon;
    }

    public CustomColor getTargetColor() {
        return targetColor;
    }

    public void setDynamicCompassLocation(Supplier<Location> compassSupplier) {
        setDynamicCompassLocation(compassSupplier, Texture.WAYPOINT);
    }

    public void setDynamicCompassLocation(Supplier<Location> compassSupplier, Texture icon) {
        setDynamicCompassLocation(compassSupplier, icon, CustomColor.fromHexString("#FFFFFF"));
    }

    public void setDynamicCompassLocation(Supplier<Location> compassSupplier, Texture icon, CustomColor color) {
        if (compassSupplier == null) {
            return;
        }

        locationSupplier = compassSupplier;
        compassLocation = compassSupplier.get();
        targetIcon = icon;
        targetColor = color;
    }

    public void setCompassLocation(Location location) {
        setCompassLocation(location, Texture.WAYPOINT);
    }

    public void setCompassLocation(Location location, Texture icon) {
        setCompassLocation(location, icon, CustomColor.fromHexString("#FFFFFF"));
    }

    public void setCompassLocation(Location location, Texture icon, CustomColor color) {
        locationSupplier = () -> location;
        compassLocation = location;
        targetIcon = icon;
        targetColor = color;
    }

    public void reset() {
        compassLocation = null;
        locationSupplier = null;

        if (McUtils.mc().level != null) {
            // We can't remove the compass behavior, so arbitrarily set it to our
            // current position
            McUtils.mc().level.setDefaultSpawnPos(McUtils.player().blockPosition(), 0);
        }
    }

    @SubscribeEvent
    public void onSetSpawn(SetSpawnEvent e) {
        BlockPos spawnPos = e.getSpawnPos();

        if (McUtils.player() == null) {
            // Reset compass
            compassLocation = null;
            locationSupplier = null;

            if (McUtils.mc().level != null) {
                McUtils.mc().level.setDefaultSpawnPos(spawnPos, 0);
            }
        } else if (locationSupplier != null) {
            // If we have a set location, do not update our spawn point
            e.setCanceled(true);
        }
    }
}
