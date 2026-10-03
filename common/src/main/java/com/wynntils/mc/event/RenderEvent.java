/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.utils.type.RenderElementType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public abstract class RenderEvent extends Event {
    private final GuiGraphicsExtractor guiGraphics;
    private final DeltaTracker deltaTracker;
    private final Window window;
    private final RenderElementType type;

    protected RenderEvent(
            GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, Window window, RenderElementType type) {
        this.guiGraphics = guiGraphics;
        this.deltaTracker = deltaTracker;
        this.window = window;
        this.type = type;
    }

    public RenderElementType getType() {
        return type;
    }

    public GuiGraphicsExtractor getGuiGraphics() {
        return guiGraphics;
    }

    public DeltaTracker getDeltaTracker() {
        return deltaTracker;
    }

    public Window getWindow() {
        return window;
    }

    public static class Pre extends RenderEvent implements ICancellableEvent {
        public Pre(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, Window window, RenderElementType type) {
            super(guiGraphics, deltaTracker, window, type);
        }
    }

    public static class Post extends RenderEvent {
        public Post(
                GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, Window window, RenderElementType type) {
            super(guiGraphics, deltaTracker, window, type);
        }
    }
}
