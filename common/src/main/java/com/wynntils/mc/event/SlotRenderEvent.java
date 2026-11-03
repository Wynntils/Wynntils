/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.inventory.SlotWidget;
import net.minecraft.world.inventory.Slot;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public abstract class SlotRenderEvent extends Event {
    private final GuiGraphicsExtractor guiGraphics;
    private final SlotWidget slotWidget;

    protected SlotRenderEvent(GuiGraphicsExtractor guiGraphics, SlotWidget slotWidget) {
        this.guiGraphics = guiGraphics;
        this.slotWidget = slotWidget;
    }

    public GuiGraphicsExtractor getGuiGraphics() {
        return guiGraphics;
    }

    public SlotWidget getSlotWidget() {
        return slotWidget;
    }

    public Slot getSlot() {
        return slotWidget.slot();
    }

    public static class Pre extends SlotRenderEvent implements ICancellableEvent {
        public Pre(GuiGraphicsExtractor guiGraphics, SlotWidget slotWidget) {
            super(guiGraphics, slotWidget);
        }
    }

    public static class CountPre extends SlotRenderEvent {
        public CountPre(GuiGraphicsExtractor guiGraphics, SlotWidget slotWidget) {
            super(guiGraphics, slotWidget);
        }
    }

    public static class Post extends SlotRenderEvent {
        public Post(GuiGraphicsExtractor guiGraphics, SlotWidget slotWidget) {
            super(guiGraphics, slotWidget);
        }
    }
}
