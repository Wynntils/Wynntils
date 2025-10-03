/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.events.BaseEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public abstract class HotbarSlotRenderEvent extends BaseEvent {
    private final GuiGraphics guiGraphics;
    private final ItemStack itemStack;
    private final int x;
    private final int y;

    protected HotbarSlotRenderEvent(GuiGraphics guiGraphics, ItemStack itemStack, int x, int y) {
        this.guiGraphics = guiGraphics;
        this.itemStack = itemStack;
        this.x = x;
        this.y = y;
    }

    public GuiGraphics getGuiGraphics() {
        return guiGraphics;
    }

    public PoseStack getPoseStack() {
        return guiGraphics.pose();
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

    public static final class Pre extends HotbarSlotRenderEvent {
        public Pre(GuiGraphics guiGraphics, ItemStack itemStack, int x, int y) {
            super(guiGraphics, itemStack, x, y);
        }
    }

    public static final class CountPre extends HotbarSlotRenderEvent {
        public CountPre(GuiGraphics guiGraphics, ItemStack itemStack, int x, int y) {
            super(guiGraphics, itemStack, x, y);
        }
    }

    public static final class Post extends HotbarSlotRenderEvent {
        public Post(GuiGraphics guiGraphics, ItemStack itemStack, int x, int y) {
            super(guiGraphics, itemStack, x, y);
        }
    }
}
