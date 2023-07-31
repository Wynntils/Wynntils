/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.beacons.type;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;

public final class VerifiedBeacon {
    private final List<Entity> entities;
    private final BeaconColor color;

    private Position position;

    private VerifiedBeacon(Position position, BeaconColor beaconColor, List<Entity> entities) {
        this.position = position;
        this.color = beaconColor;
        this.entities = ImmutableList.copyOf(entities);
    }

    public static VerifiedBeacon fromUnverifiedBeacon(UnverifiedBeacon unverifiedBeacon, BeaconColor beaconColor) {
        return new VerifiedBeacon(unverifiedBeacon.getPosition(), beaconColor, unverifiedBeacon.getEntities());
    }

    public Entity getBaseEntity() {
        return entities.get(0);
    }

    public Position getPosition() {
        return position;
    }

    public BeaconColor getColor() {
        return color;
    }

    public void updatePosition(Position newPosition) {
        position = newPosition;
    }
}
