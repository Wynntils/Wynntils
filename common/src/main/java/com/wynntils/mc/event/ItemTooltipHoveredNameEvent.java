/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public class ItemTooltipHoveredNameEvent extends Event {
    private Component hoveredName;
    private final ItemStack itemStack;

    public ItemTooltipHoveredNameEvent(Component hoveredName, ItemStack itemStack) {
        this.hoveredName = hoveredName;
        this.itemStack = itemStack;
    }

    public Component getHoveredName() {
        return hoveredName;
    }

    public void setHoveredName(Component hoveredName) {
        this.hoveredName = hoveredName;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
