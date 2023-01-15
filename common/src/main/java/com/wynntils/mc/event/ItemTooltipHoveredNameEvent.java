/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.WynntilsEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ItemTooltipHoveredNameEvent extends WynntilsEvent {
    private Component hoveredName;
    private final ItemStack stack;

    public ItemTooltipHoveredNameEvent(Component hoveredName, ItemStack stack) {
        this.hoveredName = hoveredName;
        this.stack = stack;
    }

    public Component getHoveredName() {
        return hoveredName;
    }

    public void setHoveredName(Component hoveredName) {
        this.hoveredName = hoveredName;
    }

    public ItemStack getStack() {
        return stack;
    }
}
