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

public abstract class RemoveEntitiesEvent extends Event {
    public static class Pre extends RemoveEntitiesEvent {
        private final List<Entity> entities;

        public Pre(ClientboundRemoveEntitiesPacket packet) {
            this.entities = new ArrayList<>();
            for (int id : packet.entityIds()) {
                this.entities.add(McUtils.mc().level.getEntity(id));
            }
        }

        public List<Entity> getEntities() {
            return entities;
        }
    }

    public static class Post extends RemoveEntitiesEvent {
        private final List<Integer> entityIds;

        public Post(ClientboundRemoveEntitiesPacket packet) {
            this.entityIds = packet.entityIds();
        }

        public List<Integer> getEntityIds() {
            return entityIds;
        }
    }
}
