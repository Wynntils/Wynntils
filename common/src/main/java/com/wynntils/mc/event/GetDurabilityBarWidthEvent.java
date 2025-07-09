/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public class GetDurabilityBarWidthEvent extends Event {
    private final ItemStack stack;
    private int width;

    public GetDurabilityBarWidthEvent(ItemStack stack, int width) {
        this.stack = stack;
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public ItemStack getStack() {
        return stack;
    }
}
