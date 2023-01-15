/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.WynntilsEvent;
import net.minecraft.world.item.ItemStack;

public abstract class HotbarSlotRenderEvent extends WynntilsEvent {
    private final ItemStack stack;
    private final int x;
    private final int y;

    protected HotbarSlotRenderEvent(ItemStack stack, int x, int y) {
        this.stack = stack;
        this.x = x;
        this.y = y;
    }

    public ItemStack getStack() {
        return stack;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public static class Pre extends HotbarSlotRenderEvent {
        public Pre(ItemStack stack, int x, int y) {
            super(stack, x, y);
        }
    }

    public static class Post extends HotbarSlotRenderEvent {
        public Post(ItemStack stack, int x, int y) {
            super(stack, x, y);
        }
    }
}
