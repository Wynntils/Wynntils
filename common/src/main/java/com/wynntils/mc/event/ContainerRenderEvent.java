/*
 * Copyright © Wynntils 2021-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.bus.api.Event;

/** Fired on inventory render */
public class ContainerRenderEvent extends Event {
    private final AbstractContainerScreen<?> screen;
    private final GuiGraphicsExtractor guiGraphics;

    public ContainerRenderEvent(AbstractContainerScreen<?> screen, GuiGraphicsExtractor guiGraphics) {
        this.screen = screen;
        this.guiGraphics = guiGraphics;
    }

    public AbstractContainerScreen<?> getScreen() {
        return screen;
    }

    public GuiGraphicsExtractor getGuiGraphics() {
        return guiGraphics;
    }

    public int getMouseX() {
        return guiGraphics.mouseX();
    }

    public int getMouseY() {
        return guiGraphics.mouseY();
    }
}
