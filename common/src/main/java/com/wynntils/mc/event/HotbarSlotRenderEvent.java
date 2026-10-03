/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public abstract class HotbarSlotRenderEvent extends Event {
    private final GuiGraphicsExtractor guiGraphics;
    private final ItemStack itemStack;
    private final int x;
    private final int y;

    protected HotbarSlotRenderEvent(GuiGraphicsExtractor guiGraphics, ItemStack itemStack, int x, int y) {
        this.guiGraphics = guiGraphics;
        this.itemStack = itemStack;
        this.x = x;
        this.y = y;
    }

    public GuiGraphicsExtractor getGuiGraphics() {
        return guiGraphics;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public static class Pre extends HotbarSlotRenderEvent implements ICancellableEvent {
        public Pre(GuiGraphicsExtractor guiGraphics, ItemStack itemStack, int x, int y) {
            super(guiGraphics, itemStack, x, y);
        }
    }

    public static class CountPre extends HotbarSlotRenderEvent {
        public CountPre(GuiGraphicsExtractor guiGraphics, ItemStack itemStack, int x, int y) {
            super(guiGraphics, itemStack, x, y);
        }
    }

    public static class Post extends HotbarSlotRenderEvent {
        public Post(GuiGraphicsExtractor guiGraphics, ItemStack itemStack, int x, int y) {
            super(guiGraphics, itemStack, x, y);
        }
    }
}
