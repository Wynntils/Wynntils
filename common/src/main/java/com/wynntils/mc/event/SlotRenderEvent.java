/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.eventbus.api.Event;

public abstract class SlotRenderEvent extends Event {
    private final PoseStack poseStack;
    private final Screen screen;
    private final Slot slot;

    protected SlotRenderEvent(PoseStack poseStack, Screen screen, Slot slot) {
        this.poseStack = poseStack;
        this.screen = screen;
        this.slot = slot;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public Screen getScreen() {
        return screen;
    }

    public Slot getSlot() {
        return slot;
    }

    public static class Pre extends SlotRenderEvent {
        public Pre(PoseStack poseStack, Screen screen, Slot slot) {
            super(poseStack, screen, slot);
        }
    }

    public static class CountPre extends SlotRenderEvent {
        public CountPre(PoseStack poseStack, Screen screen, Slot slot) {
            super(poseStack, screen, slot);
        }
    }

    public static class Post extends SlotRenderEvent {
        public Post(PoseStack poseStack, Screen screen, Slot slot) {
            super(poseStack, screen, slot);
        }
    }
}
