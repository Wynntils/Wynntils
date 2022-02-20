/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

/** Fired for Container Content events */
public class ItemsReceivedEvent extends Event {
    private final int containerId;
    private final int stateId;
    private final List<ItemStack> items;
    private final ItemStack carriedItem;

    public ItemsReceivedEvent(
            int containerId, int stateId, List<ItemStack> items, ItemStack carriedItem) {
        this.containerId = containerId;
        this.stateId = stateId;
        this.items = items;
        this.carriedItem = carriedItem;
    }

    public int getContainerId() {
        return containerId;
    }

    public int getStateId() {
        return stateId;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public ItemStack getCarriedItem() {
        return carriedItem;
    }
}
