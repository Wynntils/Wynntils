/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public abstract class ContainerSetSlotEvent extends Event {
    private final int containerId;
    private final int stateId;
    private final int slot;
    private final ItemStack itemStack;

    protected ContainerSetSlotEvent(int containerId, int stateId, int slot, ItemStack itemStack) {
        this.containerId = containerId;
        this.stateId = stateId;
        this.slot = slot;
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getContainerId() {
        return containerId;
    }

    public int getSlot() {
        return slot;
    }

    public int getStateId() {
        return stateId;
    }

    /**
     * Note: This event does not go through {@link com.wynntils.handlers.item.ItemHandler},
     *       so you cannot use it to get {@link com.wynntils.models.items.WynnItem}s.
     *       Use {@link Post} instead.
     */
    public static class Pre extends ContainerSetSlotEvent {
        public Pre(int containerId, int stateId, int slot, ItemStack itemStack) {
            super(containerId, stateId, slot, itemStack);
        }
    }

    /**
     * Note: This is called after {@link SetSlotEvent.Pre}, so you can use it to get {@link com.wynntils.models.items.WynnItem}s.
     */
    public static class Post extends ContainerSetSlotEvent {
        public Post(int containerId, int stateId, int slot, ItemStack itemStack) {
            super(containerId, stateId, slot, itemStack);
        }
    }
}
