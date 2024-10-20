/*
 * Copyright © Wynntils 2021-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class ContainerLimitStackSizeEvent extends Event implements ICancellableEvent {
    private final Container container;
    private final int slotNum;
    private final ItemStack itemStack;
    private int limit;

    public ContainerLimitStackSizeEvent(Container container, int slotNum, ItemStack itemStack, int limit) {
        this.container = container;
        this.slotNum = slotNum;
        this.itemStack = itemStack;
        this.limit = limit;
    }

    public Container getContainer() {
        return container;
    }

    public int getSlotNum() {
        return slotNum;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
