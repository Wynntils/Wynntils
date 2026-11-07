/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public class ItemStackSplitEvent extends Event {
    private final ItemStack source;
    private final ItemStack result;

    public ItemStackSplitEvent(ItemStack source, ItemStack result) {
        this.source = source;
        this.result = result;
    }

    public ItemStack getSource() {
        return source;
    }

    public ItemStack getResult() {
        return result;
    }
}
