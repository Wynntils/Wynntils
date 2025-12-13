/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.colorpicker.widgets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.screens.colorpicker.ColorPickerScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.RenderDirection;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class SaturationBrightnessWidget extends AbstractWidget {
    private final ColorPickerScreen colorPickerScreen;

    private boolean cursorHeld = false;
    private CustomColor color;
    private int cursorX;
    private int cursorY;

    public SaturationBrightnessWidget(
            int x, int y, int width, int height, ColorPickerScreen colorPickerScreen, CustomColor baseColor) {
        super(x, y, width, height, Component.literal("Saturation Brightness Widget"));

        this.colorPickerScreen = colorPickerScreen;

        float[] hsbColor = baseColor.asHSB();

        color = CustomColor.fromHSV(hsbColor[0], 1.0f, 1.0f, 1.0f);

        cursorX = (int) Mth.clamp(width * hsbColor[1], 0, width);
        cursorY = (int) Mth.clamp(height * (1.0f - hsbColor[2]), 0, height);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.fillGradient(
                guiGraphics,
                getX(),
                getY(),
                getX() + width,
                getY() + height,
                CommonColors.WHITE,
                color.withAlpha(255),
                RenderDirection.HORIZONTAL);
        RenderUtils.fillGradient(
                guiGraphics,
                getX(),
                getY(),
                getX() + width,
                getY() + height,
                CommonColors.WHITE.withAlpha(0),
                CommonColors.BLACK,
                RenderDirection.VERTICAL);

        RenderUtils.drawRectBorders(
                guiGraphics,
                CommonColors.BLACK,
                getX() + cursorX - 2,
                getY() + cursorY - 2,
                getX() + cursorX + 2,
                getY() + cursorY + 2,
                1);

        if (this.isHovered) {
            guiGraphics.requestCursor(cursorHeld ? CursorTypes.RESIZE_ALL : CursorTypes.POINTING_HAND);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!isMouseOver(event.x(), event.y())) return false;

        cursorX = (int) (event.x() - getX());
        cursorY = (int) (event.y() - getY());

        cursorHeld = true;

        updateValue(cursorX, cursorY);

        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (!cursorHeld) return false;

        cursorX = Mth.clamp((int) (event.x() - getX()), 0, getWidth());
        cursorY = Mth.clamp((int) (event.y() - getY()), 0, getHeight());

        updateValue(cursorX, cursorY);

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        cursorHeld = false;

        return super.mouseReleased(event);
    }

    public void setColor(CustomColor color) {
        this.color = color;
    }

    public void updateCursor(float saturation, float brightness) {
        cursorX = (int) Mth.clamp(width * saturation, 0, width);
        cursorY = (int) Mth.clamp(height * (1.0f - brightness), 0, height);
    }

    private void updateValue(double x, double y) {
        float newSaturation = (float) Mth.clamp(x / getWidth(), 0.0f, 1.0f);
        float newBrightness = (float) (1.0f - Mth.clamp(y / getHeight(), 0.0f, 1.0f));

        colorPickerScreen.setSaturation(newSaturation);
        colorPickerScreen.setBrightness(newBrightness);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
