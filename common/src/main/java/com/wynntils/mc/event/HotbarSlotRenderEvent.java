/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public abstract class HotbarSlotRenderEvent extends Event {
    private final ItemStack itemStack;
    private final int x;
    private final int y;

    protected HotbarSlotRenderEvent(ItemStack itemStack, int x, int y) {
        this.itemStack = itemStack;
        this.x = x;
        this.y = y;
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
        public Pre(ItemStack itemStack, int x, int y) {
            super(itemStack, x, y);
        }
    }

    public static class CountPre extends HotbarSlotRenderEvent {
        public CountPre(ItemStack itemStack, int x, int y) {
            super(itemStack, x, y);
        }
    }

    public static class Post extends HotbarSlotRenderEvent {
        public Post(ItemStack itemStack, int x, int y) {
            super(itemStack, x, y);
        }
    }
}
