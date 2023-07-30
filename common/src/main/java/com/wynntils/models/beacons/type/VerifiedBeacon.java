/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.beacons.type;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class VerifiedBeacon {
    private final List<Entity> entities;
    private final BeaconColor color;

    private Vec3 position;

    private VerifiedBeacon(Vec3 position, List<Entity> entities) {
        this.position = position;
        this.color = BeaconColor.fromEntity(entities.get(0));
        this.entities = ImmutableList.copyOf(entities);
    }

    public static VerifiedBeacon fromUnverifiedBeacon(UnverifiedBeacon unverifiedBeacon) {
        return new VerifiedBeacon(unverifiedBeacon.getPosition(), unverifiedBeacon.getEntities());
    }

    public Entity getBaseEntity() {
        return entities.get(0);
    }

    public Vec3 getPosition() {
        return position;
    }

    public BeaconColor getColor() {
        return color;
    }

    public void updatePosition(Vec3 newPosition) {
        position = newPosition;
    }
}
