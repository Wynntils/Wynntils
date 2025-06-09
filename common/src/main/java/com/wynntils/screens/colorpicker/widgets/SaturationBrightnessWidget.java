/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.colorpicker.widgets;

import com.wynntils.screens.colorpicker.ColorPickerScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
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
        RenderUtils.fillSidewaysGradient(
                guiGraphics.pose(),
                getX(),
                getY(),
                getX() + width,
                getY() + height,
                0,
                CommonColors.WHITE,
                color.withAlpha(255));
        RenderUtils.fillGradient(
                guiGraphics.pose(),
                getX(),
                getY(),
                getX() + width,
                getY() + height,
                0,
                CommonColors.WHITE.withAlpha(0),
                CommonColors.BLACK);

        RenderUtils.drawRectBorders(
                guiGraphics.pose(),
                CommonColors.BLACK,
                getX() + cursorX - 2,
                getY() + cursorY - 2,
                getX() + cursorX + 2,
                getY() + cursorY + 2,
                2,
                1);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        cursorX = (int) (mouseX - getX());
        cursorY = (int) (mouseY - getY());

        cursorHeld = true;

        updateValue(cursorX, cursorY);

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!cursorHeld) return false;

        cursorX = Mth.clamp((int) (mouseX - getX()), 0, getWidth());
        cursorY = Mth.clamp((int) (mouseY - getY()), 0, getHeight());

        updateValue(cursorX, cursorY);

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        cursorHeld = false;

        return super.mouseReleased(mouseX, mouseY, button);
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
