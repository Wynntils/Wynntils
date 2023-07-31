/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.compass;

import com.wynntils.core.components.Model;
import com.wynntils.mc.event.SetSpawnEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.services.map.pois.WaypointPoi;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.mc.type.PoiLocation;
import com.wynntils.utils.render.Texture;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class CompassModel extends Model {
    public static final Location WORLD_SPAWN = new Location(-1572, 41, -1668);
    public static final Location HUB_SPAWN = new Location(295, 34, 321);
    private Supplier<Location> locationSupplier = null;
    private Location compassLocation = null; // this field acts as a cache for the supplier
    private Texture targetIcon = null;
    private CustomColor targetColor = null;
    private Location spawnTracker;

    public CompassModel() {
        super(List.of());
    }

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
        if (locationSupplier == null) return Optional.empty();

        Location location = locationSupplier.get();
        if (location == null) return Optional.empty();

        WaypointPoi waypointPoi = new WaypointPoi(() -> PoiLocation.fromLocation(location));
        return Optional.of(waypointPoi);
    }

    public Texture getTargetIcon() {
        return targetIcon;
    }

    public CustomColor getTargetColor() {
        return targetColor;
    }

    public Location getSpawnTracker() {
        return spawnTracker;
    }

    public void setCompassToSpawnTracker() {
        setDynamicCompassLocation(() -> spawnTracker, Texture.WAYPOINT);
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
        Location spawn = new Location(e.getSpawnPos());
        if (spawn.equals(WORLD_SPAWN) || spawn.equals(HUB_SPAWN)) {
            spawnTracker = null;
            return;
        }

        var player = Location.containing(McUtils.player().position());
        if (spawn.equals(player)) {
            // Wynncraft "resets" tracking by setting the compass to your current
            // location. In theory, this can fail if you happen to be standing on
            // the spot that is the target of the activity you start tracking...
            spawnTracker = null;
            return;
        }

        spawnTracker = spawn;
    }
}
