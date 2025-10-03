/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.CancelRequestable;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.level.entity.EntityAccess;

public final class AddEntityLookupEvent extends BaseEvent implements CancelRequestable {
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
