/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.utils.mc.McUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;

public class RemoveEntitiesEvent extends Event {
    private final List<Integer> entityIds;

    public RemoveEntitiesEvent(ClientboundRemoveEntitiesPacket packet) {
        this.entityIds = packet.getEntityIds();
    }

    public List<Integer> getEntityIds() {
        return entityIds;
    }

    public static class Pre extends Event {
        private final List<Entity> entities;

        public Pre(ClientboundRemoveEntitiesPacket packet) {
            this.entities = new ArrayList<>();
            for (int id : packet.getEntityIds()) {
                this.entities.add(McUtils.mc().level.getEntity(id));
            }
        }

        public List<Entity> getEntities() {
            return entities;
        }
    }
}
