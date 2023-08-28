/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public abstract class ContainerLabelRenderEvent extends Event {
    private final AbstractContainerScreen<?> screen;
    private final PoseStack poseStack;
    private final float x;
    private final float y;

    private int color;

    protected ContainerLabelRenderEvent(
            AbstractContainerScreen<?> screen, PoseStack poseStack, float x, float y, int color) {
        this.screen = screen;
        this.poseStack = poseStack;
        this.x = x;
        this.y = y;

        this.color = color;
    }

    public AbstractContainerScreen<?> getScreen() {
        return screen;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public static class ContainerLabel extends ContainerLabelRenderEvent {
        private Component containerLabel;

        public ContainerLabel(
                AbstractContainerScreen<?> screen,
                PoseStack poseStack,
                int color,
                float x,
                float y,
                Component containerLabel) {
            super(screen, poseStack, x, y, color);
            this.containerLabel = containerLabel;
        }

        public Component getContainerLabel() {
            return containerLabel;
        }

        public void setContainerLabel(Component containerLabel) {
            this.containerLabel = containerLabel;
        }
    }

    public static class InventoryLabel extends ContainerLabelRenderEvent {
        private Component inventoryLabel;

        public InventoryLabel(
                AbstractContainerScreen<?> screen,
                PoseStack poseStack,
                int color,
                float x,
                float y,
                Component inventoryLabel) {
            super(screen, poseStack, x, y, color);
            this.inventoryLabel = inventoryLabel;
        }

        public Component getInventoryLabel() {
            return inventoryLabel;
        }

        public void setInventoryLabel(Component inventoryLabel) {
            this.inventoryLabel = inventoryLabel;
        }
    }
}
