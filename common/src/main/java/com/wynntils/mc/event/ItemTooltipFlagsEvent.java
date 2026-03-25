/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.Event;

public class ItemTooltipFlagsEvent extends Event {
    private final ItemStack itemStack;
    private TooltipFlag flags;

    public ItemTooltipFlagsEvent(ItemStack itemStack, TooltipFlag flags) {
        this.itemStack = itemStack;
        this.flags = flags;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public TooltipFlag getFlags() {
        return flags;
    }

    public void setFlags(TooltipFlag flags) {
        this.flags = flags;
    }
}
