/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;

public class GetCameraEntityEvent extends Event {
    private Entity entity;

    public GetCameraEntityEvent(Entity entity) {
        this.entity = entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
