/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import java.util.List;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;

public final class RemoveEntitiesEvent extends BaseEvent {
    private final List<Integer> entityIds;

    public RemoveEntitiesEvent(ClientboundRemoveEntitiesPacket packet) {
        this.entityIds = packet.getEntityIds();
    }

    public List<Integer> getEntityIds() {
        return entityIds;
    }
}
