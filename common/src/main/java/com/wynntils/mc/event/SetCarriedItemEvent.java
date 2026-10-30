/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

// This is fired when the item you pick up in a container is changed, not the item in your hand
public class SetCarriedItemEvent extends Event {
    private final ItemStack previous;
    private final ItemStack carried;

    public SetCarriedItemEvent(ItemStack previous, ItemStack carried) {
        this.previous = previous;
        this.carried = carried;
    }

    public ItemStack getPrevious() {
        return previous;
    }

    public ItemStack getCarried() {
        return carried;
    }
}
