/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.labels.event;

import com.wynntils.core.text.StyledText;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Event;

public class EntityLabelChangedEvent extends Event {
    private final Entity entity;
    private final StyledText name;
    private final StyledText oldName;

    public EntityLabelChangedEvent(Entity entity, StyledText name, StyledText oldName) {
        this.entity = entity;
        this.name = name;
        this.oldName = oldName;
    }

    public Entity getEntity() {
        return entity;
    }

    public StyledText getName() {
        return name;
    }

    public StyledText getOldName() {
        return oldName;
    }
}
