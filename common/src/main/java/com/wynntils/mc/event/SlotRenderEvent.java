/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.eventbus.api.Event;

public abstract class SlotRenderEvent extends Event {
    private final Screen screen;
    private final Slot slot;

    protected SlotRenderEvent(Screen screen, Slot slot) {
        this.screen = screen;
        this.slot = slot;
    }

    public Screen getScreen() {
        return screen;
    }

    public Slot getSlot() {
        return slot;
    }

    public static class Pre extends SlotRenderEvent {
        public Pre(Screen screen, Slot slot) {
            super(screen, slot);
        }
    }

    public static class CountPre extends SlotRenderEvent {
        public CountPre(Screen screen, Slot slot) {
            super(screen, slot);
        }
    }

    public static class Post extends SlotRenderEvent {
        public Post(Screen screen, Slot slot) {
            super(screen, slot);
        }
    }
}
