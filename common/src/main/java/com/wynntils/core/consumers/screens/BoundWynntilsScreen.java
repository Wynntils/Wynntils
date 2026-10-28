/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.screens;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * A screen that is laid out for a fixed size. If that size does not fit in the player's GUI scale, the content is
 * drawn at the largest GUI scale that does, with mouse input converted to match.
 */
public abstract class BoundWynntilsScreen extends WynntilsScreen {
    private double contentScale = 1.0;
    private int realMouseX;
    private int realMouseY;

    protected BoundWynntilsScreen(Component component) {
        super(component);
    }

    protected abstract int getDesignWidth();

    protected abstract int getDesignHeight();

    @Override
    protected void doInit() {
        updateContentScale();

        super.doInit();
    }

    @Override
    public final void doRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        realMouseX = mouseX;
        realMouseY = mouseY;

        if (contentScale == 1.0) {
            doBoundedRender(guiGraphics, mouseX, mouseY, partialTick);
            return;
        }

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale((float) contentScale, (float) contentScale);

        try {
            doBoundedRender(guiGraphics, (int) (mouseX / contentScale), (int) (mouseY / contentScale), partialTick);
        } finally {
            guiGraphics.pose().popMatrix();
        }
    }

    public void doBoundedRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.doRender(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public final boolean doMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return doBoundedMouseClicked(toContentSpace(event), isDoubleClick);
    }

    public boolean doBoundedMouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return super.doMouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return super.mouseReleased(toContentSpace(event));
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return super.mouseDragged(toContentSpace(event), dragX / contentScale, dragY / contentScale);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        return super.mouseScrolled(mouseX / contentScale, mouseY / contentScale, deltaX, deltaY);
    }

    public int getRealMouseX() {
        return realMouseX;
    }

    public int getRealMouseY() {
        return realMouseY;
    }

    public double getContentScale() {
        return contentScale;
    }

    public void renderInRealSpace(GuiGraphics guiGraphics, Runnable draw) {
        if (contentScale == 1.0) {
            draw.run();
            return;
        }

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale((float) (1.0 / contentScale), (float) (1.0 / contentScale));
        draw.run();
        guiGraphics.pose().popMatrix();
    }

    private void updateContentScale() {
        Window window = McUtils.window();
        int guiScale = window.getGuiScale();
        int fittingScale = Math.min(window.getWidth() / getDesignWidth(), window.getHeight() / getDesignHeight());
        int effectiveScale = Mth.clamp(fittingScale, 1, guiScale);

        contentScale = (double) effectiveScale / guiScale;

        this.width = (int) Math.ceil(window.getWidth() / (double) effectiveScale);
        this.height = (int) Math.ceil(window.getHeight() / (double) effectiveScale);
    }

    private MouseButtonEvent toContentSpace(MouseButtonEvent event) {
        if (contentScale == 1.0) {
            return event;
        }

        return new MouseButtonEvent(event.x() / contentScale, event.y() / contentScale, event.buttonInfo());
    }
}
