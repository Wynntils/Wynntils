/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraftforge.eventbus.api.Event;

public class RemoveEntitiesEvent extends Event {
    private final IntList entityIds;

    public RemoveEntitiesEvent(ClientboundRemoveEntitiesPacket packet) {
        this.entityIds = packet.getEntityIds();
    }

    public IntList getEntityIds() {
        return entityIds;
    }
}
