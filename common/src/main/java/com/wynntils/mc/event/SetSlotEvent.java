/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

/** Fired when an item is set in a slot */
public abstract class SetSlotEvent extends Event {
    private final Container container;
    private final int slot;
    private final ItemStack itemStack;

    protected SetSlotEvent(Container container, int slot, ItemStack itemStack) {
        this.container = container;
        this.slot = slot;
        this.itemStack = itemStack;
    }

    public Container getContainer() {
        return container;
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public static class Pre extends SetSlotEvent {
        public Pre(Container container, int slot, ItemStack itemStack) {
            super(container, slot, itemStack);
        }
    }

    public static class Post extends SetSlotEvent {
        public Post(Container container, int slot, ItemStack item) {
            super(container, slot, item);
        }
    }
}
