/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.beacons.type;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class UnverifiedBeacon {
    private static final float POSITION_OFFSET_Y = 7.5f;

    private final Vec3 position;
    private final List<Entity> entities = new ArrayList<>();

    public UnverifiedBeacon(Vec3 position, Entity entity) {
        this.position = position;
        entities.add(entity);
    }

    public Vec3 getPosition() {
        return position;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public boolean addEntity(Entity entity) {
        Vec3 entityPosition = entity.position();
        Vec3 lastEntityPosition = entities.get(entities.size() - 1).position();

        if (entityPosition.y() - lastEntityPosition.y() == POSITION_OFFSET_Y) {
            entities.add(entity);
            return true;
        }

        return false;
    }
}
