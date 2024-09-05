/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.colorpicker.widgets;

import com.wynntils.screens.colorpicker.ColorPickerScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class AlphaSlider extends AbstractSliderButton {
    private final ColorPickerScreen colorPickerScreen;

    public AlphaSlider(int x, int y, int width, int height, double value, ColorPickerScreen colorPickerScreen) {
        super(x, y, width, height, Component.literal(""), value);

        this.colorPickerScreen = colorPickerScreen;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.fillSidewaysGradient(
                guiGraphics.pose(),
                getX(),
                getY(),
                getX() + width,
                getY() + height,
                0,
                CommonColors.WHITE.withAlpha(0),
                colorPickerScreen.getColor().withAlpha(255));

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
        int newAlpha = (int) ((mouseX - getX()) / getWidth() * 255);

        colorPickerScreen.setAlpha(Mth.clamp(newAlpha, 0, 255));
        setValue((mouseX - (this.getX() + 0.5)) / (double) (this.width - 1));
    }
}
