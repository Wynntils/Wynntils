/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.beacons.type;

import com.google.common.collect.ImmutableList;
import com.wynntils.utils.mc.type.Location;
import java.util.List;
import net.minecraft.world.entity.Entity;

public final class Beacon {
    private final List<Entity> entities;
    private Location location;
    private final BeaconColor color;

    public Beacon(Location location, BeaconColor beaconColor, List<Entity> entities) {
        this.location = location;
        this.color = beaconColor;
        this.entities = ImmutableList.copyOf(entities);
    }

    public Entity getBaseEntity() {
        return entities.get(0);
    }

    public Location getLocation() {
        return location;
    }

    public BeaconColor getColor() {
        return color;
    }

    public void updateLocation(Location newLocation) {
        location = newLocation;
    }
}
