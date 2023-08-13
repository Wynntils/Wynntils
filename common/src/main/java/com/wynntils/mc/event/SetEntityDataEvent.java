/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import java.util.List;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraftforge.eventbus.api.Event;

public class SetEntityDataEvent extends Event {
    private final int id;
    private final List<SynchedEntityData.DataValue<?>> packedItems;

    public SetEntityDataEvent(ClientboundSetEntityDataPacket packet) {
        this.id = packet.id();
        this.packedItems = packet.packedItems();
    }

    public int getId() {
        return id;
    }

    public List<SynchedEntityData.DataValue<?>> getPackedItems() {
        return packedItems;
    }
}
