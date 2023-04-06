/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.labels.event;

import com.wynntils.core.text.StyledText2;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Event;

public class EntityLabelChangedEvent extends Event {
    private final Entity entity;
    private final StyledText2 name;
    private final StyledText2 oldName;

    public EntityLabelChangedEvent(Entity entity, StyledText2 name, StyledText2 oldName) {
        this.entity = entity;
        this.name = name;
        this.oldName = oldName;
    }

    public Entity getEntity() {
        return entity;
    }

    public StyledText2 getName() {
        return name;
    }

    public StyledText2 getOldName() {
        return oldName;
    }
}
