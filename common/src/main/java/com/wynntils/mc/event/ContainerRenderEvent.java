/*
 * Copyright © Wynntils 2021-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.eventbus.api.Event;

/** Fired on inventory render */
public class ContainerRenderEvent extends Event {
    private final AbstractContainerScreen<?> screen;
    private final PoseStack poseStack;
    private final int mouseX;
    private final int mouseY;
    private final float partialTicks;
    private final Slot hoveredSlot;

    public ContainerRenderEvent(
            AbstractContainerScreen<?> screen,
            PoseStack poseStack,
            int mouseX,
            int mouseY,
            float partialTicks,
            Slot hoveredSlot) {
        this.screen = screen;
        this.poseStack = poseStack;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.partialTicks = partialTicks;
        this.hoveredSlot = hoveredSlot;
    }

    public AbstractContainerScreen<?> getScreen() {
        return screen;
    }

    public PoseStack getPoseStack() {
        return poseStack;
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
