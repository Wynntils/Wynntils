/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

/**
 * The name of this class is anti-pattern to stay in line with {@link GetDurabilityBarColorEvent} and {@link GetDurabilityBarWidthEvent}
 */
public class GetDurabilityBarVisibilityEvent extends Event {
    private final ItemStack stack;
    private boolean visible;

    public GetDurabilityBarVisibilityEvent(ItemStack stack, boolean visible) {
        this.stack = stack;
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public ItemStack getStack() {
        return stack;
    }
}
