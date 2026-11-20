/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * This event is fired while the item stack and editable component lines are available, before the tooltip is queued for extraction.
 * <p>
 * There are 3 places we fire this from. The first is in {@link SlotWidgetMixin}, which is where the majority of calls happen.
 * The second is in {@link GuiGraphicsExtractorMixin} which deals with chat items.
 * And the third is in {@link NeoForgeGuiGraphicsExtractorMixin} which handles the patched version from NeoForge.
 * </p>
 */
public final class ItemTooltipRenderEvent extends Event implements ICancellableEvent {
    private final GuiGraphicsExtractor guiGraphics;
    private final ItemStack itemStack;

    private List<Component> tooltips;

    public ItemTooltipRenderEvent(GuiGraphicsExtractor guiGraphics, ItemStack itemStack, List<Component> tooltips) {
        this.guiGraphics = guiGraphics;
        this.itemStack = itemStack;
        this.tooltips = tooltips;
    }

    public GuiGraphicsExtractor getGuiGraphics() {
        return guiGraphics;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public List<Component> getTooltips() {
        return tooltips;
    }

    public void setTooltips(List<Component> tooltips) {
        this.tooltips = Collections.unmodifiableList(tooltips);
    }
}
