/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.eventbus.api.Event;

public abstract class RenderEvent extends Event {
    private final ElementType type;
    private final PoseStack poseStack;
    private final int mouseX;
    private final int mouseY;
    private final float partialTick;

    public RenderEvent(ElementType type, PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.type = type;
        this.poseStack = poseStack;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.partialTick = partialTick;
    }

    public ElementType getType() {
        return type;
    }

    public float getPartialTick() {
        return partialTick;
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public enum ElementType {}

    public static class Pre extends RenderEvent {

        public Pre(ElementType type, PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            super(type, poseStack, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean isCancelable() {
            return true;
        }
    }

    public static class Post extends RenderEvent {

        public Post(ElementType type, PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            super(type, poseStack, mouseX, mouseY, partialTick);
        }
    }
}
