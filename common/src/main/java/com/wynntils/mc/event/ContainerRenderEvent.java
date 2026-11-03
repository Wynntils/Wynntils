/*
 * Copyright © Wynntils 2021-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.inventory.SlotWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.bus.api.Event;

/** Fired on inventory render */
public class ContainerRenderEvent extends Event {
    private final AbstractContainerScreen<?> screen;
    private final GuiGraphicsExtractor guiGraphics;
    private final int mouseX;
    private final int mouseY;
    private final SlotWidget hoveredSlot;

    public ContainerRenderEvent(
            AbstractContainerScreen<?> screen,
            GuiGraphicsExtractor guiGraphics,
            int mouseX,
            int mouseY,
            SlotWidget hoveredSlot) {
        this.screen = screen;
        this.guiGraphics = guiGraphics;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.hoveredSlot = hoveredSlot;
    }

    public AbstractContainerScreen<?> getScreen() {
        return screen;
    }

    public GuiGraphicsExtractor getGuiGraphics() {
        return guiGraphics;
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public SlotWidget getHoveredSlot() {
        return hoveredSlot;
    }
}
