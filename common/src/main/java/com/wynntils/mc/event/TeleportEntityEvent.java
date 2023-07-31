/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Event;

public class TeleportEntityEvent extends Event {
    private final Entity entity;
    private final Position newPosition;

    public TeleportEntityEvent(Entity entity, Position newPosition) {
        this.entity = entity;
        this.newPosition = newPosition;
    }

    public Entity getEntity() {
        return entity;
    }

    public Position getNewPosition() {
        return newPosition;
    }
}
