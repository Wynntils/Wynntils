package com.wynntils.mc.event;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public class EntityDataSetEvent extends Event {
    private final int id;
    private final List<SynchedEntityData.DataItem<?>> packedItems;

    public EntityDataSetEvent(int id, List<SynchedEntityData.DataItem<?>> packedItems) {
        this.id = id;
        this.packedItems = packedItems;
    }

    public int getId() {
        return id;
    }

    public List<SynchedEntityData.DataItem<?>> getPackedItems() {
        return packedItems;
    }
}
