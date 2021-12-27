/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.eventbus.api.Event;

public class InventoryRenderEvent extends Event {
    private Screen screen;
    private PoseStack poseStack;
    private int mouseX;
    private int mouseY;
    private float partialTicks;
    private Slot hoveredSlot;

    public InventoryRenderEvent() {
    }

    public InventoryRenderEvent(
            Screen screen,
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

    public Screen getScreen() {
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
