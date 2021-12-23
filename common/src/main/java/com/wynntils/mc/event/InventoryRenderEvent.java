package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.framework.events.Event;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.Slot;

public class InventoryRenderEvent extends Event {
    private final Screen screen;
    private final PoseStack poseStack;
    private final int mouseX;
    private final int mouseY;
    private final float partialTicks;
    private final Slot hoveredSlot;

    public InventoryRenderEvent(Screen screen, PoseStack poseStack, int mouseX, int mouseY, float partialTicks, Slot hoveredSlot) {
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

    @Override
    public boolean isCancellable() {
        return false;
    }
}
