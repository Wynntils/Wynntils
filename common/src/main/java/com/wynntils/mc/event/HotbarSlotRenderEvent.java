/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public abstract class HotbarSlotRenderEvent extends Event {
    private final PoseStack poseStack;
    private final ItemStack itemStack;
    private final int x;
    private final int y;

    protected HotbarSlotRenderEvent(PoseStack poseStack, ItemStack itemStack, int x, int y) {
        this.poseStack = poseStack;
        this.itemStack = itemStack;
        this.x = x;
        this.y = y;
    }

    public PoseStack getPoseStack() {
        return poseStack;
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

    public static class Pre extends HotbarSlotRenderEvent {
        public Pre(PoseStack poseStack, ItemStack itemStack, int x, int y) {
            super(poseStack, itemStack, x, y);
        }
    }

    public static class CountPre extends HotbarSlotRenderEvent {
        public CountPre(PoseStack poseStack, ItemStack itemStack, int x, int y) {
            super(poseStack, itemStack, x, y);
        }
    }

    public static class Post extends HotbarSlotRenderEvent {
        public Post(PoseStack poseStack, ItemStack itemStack, int x, int y) {
            super(poseStack, itemStack, x, y);
        }
    }
}
