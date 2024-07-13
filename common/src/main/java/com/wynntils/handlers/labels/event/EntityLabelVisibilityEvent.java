/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.labels.event;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;

// Use TextDisplayChangedEvent instead, Wynn does not use armor stands for most of its labels
@Deprecated
public class EntityLabelVisibilityEvent extends Event {
    private final Entity entity;
    private final boolean value;

    public EntityLabelVisibilityEvent(Entity entity, boolean value) {
        this.entity = entity;
        this.value = value;
    }

    public Entity getEntity() {
        return entity;
    }

    public boolean getVisibility() {
        return value;
    }
}
