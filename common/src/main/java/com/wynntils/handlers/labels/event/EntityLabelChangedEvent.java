/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.labels.event;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Event;

public class EntityLabelChangedEvent extends Event {
    private final Entity entity;
    private final String name;
    private final String oldName;

    public EntityLabelChangedEvent(Entity entity, String name, String oldName) {
        this.entity = entity;
        this.name = name;
        this.oldName = oldName;
    }

    public Entity getEntity() {
        return entity;
    }

    public String getName() {
        return name;
    }

    public String getOldName() {
        return oldName;
    }
}
