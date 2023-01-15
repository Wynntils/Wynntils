/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.WynntilsEvent;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class AddEntityLookupEvent extends WynntilsEvent {
    private final UUID uuid;
    private final Map<UUID, EntityAccess> entityMap;

    public AddEntityLookupEvent(UUID uuid, Map<UUID, EntityAccess> entityMap) {
        this.uuid = uuid;
        this.entityMap = entityMap;
    }

    public UUID getUUID() {
        return uuid;
    }

    public Map<UUID, EntityAccess> getEntityMap() {
        return entityMap;
    }
}
