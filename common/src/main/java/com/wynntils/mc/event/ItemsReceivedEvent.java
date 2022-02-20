/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import java.util.List;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

/** Fired when items are received */
public class ItemsReceivedEvent extends Event {
    private final AbstractContainerMenu container;
    private final List<ItemStack> items;

    public ItemsReceivedEvent(AbstractContainerMenu container, List<ItemStack> items) {
        this.container = container;
        this.items = items;
    }

    public AbstractContainerMenu getContainer() {
        return container;
    }

    public List<ItemStack> getItems() {
        return items;
    }
}
