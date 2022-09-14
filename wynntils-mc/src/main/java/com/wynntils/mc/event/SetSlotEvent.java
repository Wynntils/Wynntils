/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/** Fired when an item is set in a slot */
@Cancelable
public class SetSlotEvent extends Event {
    private final Container container;
    private final int slot;
    private ItemStack item;

    public SetSlotEvent(Container container, int slot, ItemStack item) {
        this.container = container;
        this.slot = slot;
        this.item = item;
    }

    public Container getContainer() {
        return container;
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }
}
