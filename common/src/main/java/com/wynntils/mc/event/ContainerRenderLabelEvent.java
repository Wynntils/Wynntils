/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class ContainerRenderLabelEvent extends Event {
    private final AbstractContainerScreen<?> screen;
    private final PoseStack poseStack;
    private final int mouseX;
    private final int mouseY;

    public ContainerRenderLabelEvent(AbstractContainerScreen<?> screen, PoseStack poseStack, int mouseX, int mouseY) {
        this.screen = screen;
        this.poseStack = poseStack;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public AbstractContainerScreen<?> getScreen() {
        return screen;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }
}
