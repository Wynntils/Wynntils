/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.core.events.OperationCancelable;
import net.minecraft.world.item.ItemStack;

public abstract class ContainerSetSlotEvent extends BaseEvent {
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
     * Note: This event goes through {@link com.wynntils.handlers.item.ItemHandler},
     *       so you can use it to get {@link com.wynntils.models.items.WynnItem}s.
     */
    public static class Pre extends ContainerSetSlotEvent implements OperationCancelable {
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
