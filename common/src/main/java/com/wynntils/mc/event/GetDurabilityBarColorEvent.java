/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public class GetDurabilityBarColorEvent extends Event {
    private final ItemStack stack;
    private int color;

    public GetDurabilityBarColorEvent(ItemStack stack, int color) {
        this.stack = stack;
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public ItemStack getStack() {
        return stack;
    }
}
