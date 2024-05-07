/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.labels.event;

import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.labels.type.LabelInfo;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class EntityLabelChangedEvent extends Event {
    private final Entity entity;
    private final StyledText oldName;
    private final LabelInfo labelInfo;

    private StyledText name;

    public EntityLabelChangedEvent(Entity entity, StyledText name, StyledText oldName, LabelInfo labelInfo) {
        this.entity = entity;
        this.name = name;
        this.oldName = oldName;
        this.labelInfo = labelInfo;
    }

    public Entity getEntity() {
        return entity;
    }

    public StyledText getName() {
        return name;
    }

    public void setName(StyledText name) {
        this.name = name;
    }

    public StyledText getOldName() {
        return oldName;
    }

    public Optional<LabelInfo> getLabelInfo() {
        return Optional.ofNullable(labelInfo);
    }
}
