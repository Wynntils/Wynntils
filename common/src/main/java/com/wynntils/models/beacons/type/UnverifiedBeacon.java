/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.beacons.type;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;

public class UnverifiedBeacon {
    private static final float POSITION_OFFSET_Y = 7.5f;

    private final Position position;
    private final List<Entity> entities = new ArrayList<>();

    public UnverifiedBeacon(Position position, Entity entity) {
        this.position = position;
        entities.add(entity);
    }

    public Position getPosition() {
        return position;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public boolean addEntity(Entity entity) {
        Position entityPosition = entity.position();
        Position lastEntityPosition = entities.get(entities.size() - 1).position();

        if (entityPosition.y() - lastEntityPosition.y() == POSITION_OFFSET_Y) {
            entities.add(entity);
            return true;
        }

        return false;
    }
}
