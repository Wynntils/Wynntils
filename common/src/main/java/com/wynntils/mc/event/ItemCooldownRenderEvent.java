/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;

public class ItemCooldownRenderEvent extends BaseEvent implements ICancellableEvent {
    private final ItemStack itemStack;

    public ItemCooldownRenderEvent(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
