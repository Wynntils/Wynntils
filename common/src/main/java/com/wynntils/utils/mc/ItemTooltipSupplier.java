/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.mc;

import java.util.List;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Retains the original item tooltip data until the slot widget has a graphics extractor available. */
public final class ItemTooltipSupplier implements Function<Slot, Tooltip> {
    private final Minecraft minecraft;
    private ItemStack itemStack;
    private List<Component> tooltipLines;

    public ItemTooltipSupplier(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public Tooltip apply(Slot slot) {
        itemStack = slot.getItem();
        tooltipLines = Screen.getTooltipFromItem(minecraft, itemStack);

        return Tooltip.createForItem(tooltipLines, itemStack);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public List<Component> getTooltipLines() {
        return tooltipLines;
    }
}
