/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.labels.event;

import com.wynntils.core.text.CodedString;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Event;

public class EntityLabelChangedEvent extends Event {
    private final Entity entity;
    private final CodedString name;
    private final CodedString oldName;

    public EntityLabelChangedEvent(Entity entity, CodedString name, CodedString oldName) {
        this.entity = entity;
        this.name = name;
        this.oldName = oldName;
    }

    public Entity getEntity() {
        return entity;
    }

    public CodedString getName() {
        return name;
    }

    public CodedString getOldName() {
        return oldName;
    }
}
