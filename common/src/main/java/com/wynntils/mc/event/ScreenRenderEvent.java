/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.events.BaseEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public final class ScreenRenderEvent extends BaseEvent {
    private final Screen screen;
    private final GuiGraphics guiGraphics;
    private final int mouseX;
    private final int mouseY;
    private final float partialTick;

    public ScreenRenderEvent(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.screen = screen;
        this.guiGraphics = guiGraphics;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.partialTick = partialTick;
    }

    public GuiGraphics getGuiGraphics() {
        return guiGraphics;
    }

    public PoseStack getPoseStack() {
        return guiGraphics.pose();
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

    public Screen getScreen() {
        return screen;
    }
}
