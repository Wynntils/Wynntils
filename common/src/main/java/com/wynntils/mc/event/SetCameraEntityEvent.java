/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;

public class SetCameraEntityEvent extends Event {
    private final Entity viewingEntity;

    public SetCameraEntityEvent(Entity entity) {
        this.viewingEntity = entity;
    }

    public Entity getViewingEntity() {
        return viewingEntity;
    }
}
