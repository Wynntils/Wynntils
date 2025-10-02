/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.events.BaseEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

/** Fired on inventory render */
public class ContainerRenderEvent extends BaseEvent {
    private final AbstractContainerScreen<?> screen;
    private final GuiGraphics guiGraphics;
    private final int mouseX;
    private final int mouseY;
    private final float partialTicks;
    private final Slot hoveredSlot;

    public ContainerRenderEvent(
            AbstractContainerScreen<?> screen,
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTicks,
            Slot hoveredSlot) {
        this.screen = screen;
        this.guiGraphics = guiGraphics;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.partialTicks = partialTicks;
        this.hoveredSlot = hoveredSlot;
    }

    public AbstractContainerScreen<?> getScreen() {
        return screen;
    }

    public GuiGraphics getGuiGraphics() {
        return guiGraphics;
    }

    public PoseStack getPoseStack() {
        return guiGraphics.pose();
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public Slot getHoveredSlot() {
        return hoveredSlot;
    }
}
