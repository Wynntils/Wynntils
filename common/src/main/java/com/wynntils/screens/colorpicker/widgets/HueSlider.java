/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.colorpicker.widgets;

import com.wynntils.screens.colorpicker.ColorPickerScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class HueSlider extends AbstractSliderButton {
    private final ColorPickerScreen colorPickerScreen;

    public HueSlider(int x, int y, int width, int height, double value, ColorPickerScreen colorPickerScreen) {
        super(x, y, width, height, Component.literal(""), value);

        this.colorPickerScreen = colorPickerScreen;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (int i = 0; i < width; i++) {
            float hue = (float) i / width;
            RenderUtils.drawRect(
                    guiGraphics.pose(), CustomColor.fromHSV(hue, 1.0f, 1.0f, 1.0f), getX() + i, getY(), 1, 1, height);
        }

        double exactX = getX() + width * value;
        int handleX = (int) Math.round(exactX);

        RenderUtils.drawRectBorders(
                guiGraphics.pose(),
                CommonColors.DARK_GRAY,
                (float) (handleX - 1.0),
                (float) getY(),
                (float) (handleX + 1.0),
                (float) (getY() + getHeight()),
                2,
                1);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        updateValue(mouseX);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        updateValue(mouseX);
    }

    @Override
    protected void updateMessage() {}

    @Override
    protected void applyValue() {}

    private void updateValue(double mouseX) {
        float newHue = (float) (mouseX - getX()) / getWidth();

        colorPickerScreen.setHue(Mth.clamp(newHue, 0f, 1f));
        setValue((mouseX - (this.getX() + 0.5)) / (double) (this.width - 1));
    }
}
