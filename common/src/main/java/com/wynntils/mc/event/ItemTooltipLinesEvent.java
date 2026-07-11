/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public class ItemTooltipLinesEvent extends Event {
    private final ItemStack itemStack;
    private List<Component> tooltipLines;

    public ItemTooltipLinesEvent(ItemStack itemStack, List<Component> tooltipLines) {
        this.itemStack = itemStack;
        setTooltipLines(tooltipLines);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public List<Component> getTooltipLines() {
        return tooltipLines;
    }

    public void setTooltipLines(List<Component> tooltipLines) {
        this.tooltipLines = List.copyOf(tooltipLines);
    }
}
