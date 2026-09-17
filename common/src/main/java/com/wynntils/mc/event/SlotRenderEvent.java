/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.Slot;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public abstract class SlotRenderEvent extends Event {
    private final GuiGraphicsExtractor guiGraphics;
    private final Screen screen;
    private final Slot slot;

    protected SlotRenderEvent(GuiGraphicsExtractor guiGraphics, Screen screen, Slot slot) {
        this.guiGraphics = guiGraphics;
        this.screen = screen;
        this.slot = slot;
    }

    public GuiGraphicsExtractor getGuiGraphics() {
        return guiGraphics;
    }

    public Screen getScreen() {
        return screen;
    }

    public Slot getSlot() {
        return slot;
    }

    public static class Pre extends SlotRenderEvent implements ICancellableEvent {
        public Pre(GuiGraphicsExtractor guiGraphics, Screen screen, Slot slot) {
            super(guiGraphics, screen, slot);
        }
    }

    public static class CountPre extends SlotRenderEvent {
        public CountPre(GuiGraphicsExtractor guiGraphics, Screen screen, Slot slot) {
            super(guiGraphics, screen, slot);
        }
    }

    public static class Post extends SlotRenderEvent {
        public Post(GuiGraphicsExtractor guiGraphics, Screen screen, Slot slot) {
            super(guiGraphics, screen, slot);
        }
    }
}
