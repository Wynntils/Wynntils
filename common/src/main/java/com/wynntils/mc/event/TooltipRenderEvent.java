/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.neoforged.bus.api.Event;

/**
 * This event is fired when a tooltip is about to be rendered, when calculating the position of the tooltip.
 * You can use this event to change the positioner of the tooltip.
 */
public abstract class TooltipRenderEvent extends Event {
    private final GuiGraphics guiGraphics;

    protected TooltipRenderEvent(GuiGraphics guiGraphics) {
        this.guiGraphics = guiGraphics;
    }

    public GuiGraphics getGuiGraphics() {
        return guiGraphics;
    }

    public static class Pre extends TooltipRenderEvent {
        public Pre(GuiGraphics guiGraphics) {
            super(guiGraphics);
        }
    }

    public static class Position extends TooltipRenderEvent {
        private ClientTooltipPositioner positioner;

        public Position(GuiGraphics guiGraphics) {
            super(guiGraphics);
        }

        public ClientTooltipPositioner getPositioner() {
            return positioner;
        }

        public void setPositioner(ClientTooltipPositioner positioner) {
            this.positioner = positioner;
        }
    }

    public static class Post extends TooltipRenderEvent {
        public Post(GuiGraphics guiGraphics) {
            super(guiGraphics);
        }
    }
}
