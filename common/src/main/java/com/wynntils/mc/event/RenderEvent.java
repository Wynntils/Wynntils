/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public abstract class RenderEvent extends Event {
    private final GuiGraphics guiGraphics;
    private final float partialTicks;
    private final Window window;
    private final ElementType type;

    protected RenderEvent(GuiGraphics guiGraphics, float partialTicks, Window window, ElementType type) {
        this.guiGraphics = guiGraphics;
        this.partialTicks = partialTicks;
        this.window = window;
        this.type = type;
    }

    public ElementType getType() {
        return type;
    }

    public GuiGraphics getGuiGraphics() {
        return guiGraphics;
    }

    public PoseStack getPoseStack() {
        return guiGraphics.pose();
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public Window getWindow() {
        return window;
    }

    public enum ElementType {
        GUI, // This is called before and after Gui#render
        CROSSHAIR,
        HEALTH_BAR,
        FOOD_BAR,
        PLAYER_TAB_LIST
    }

    @Cancelable
    public static class Pre extends RenderEvent {
        public Pre(GuiGraphics guiGraphics, float partialTicks, Window window, ElementType type) {
            super(guiGraphics, partialTicks, window, type);
        }
    }

    public static class Post extends RenderEvent {
        public Post(GuiGraphics guiGraphics, float partialTicks, Window window, ElementType type) {
            super(guiGraphics, partialTicks, window, type);
        }
    }
}
