/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.colorpicker.widgets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.wynntils.screens.colorpicker.ColorPickerScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.RenderDirection;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.input.MouseButtonEvent;
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
        RenderUtils.fillGradient(
                guiGraphics,
                getX(),
                getY(),
                getX() + width,
                getY() + height,
                CommonColors.WHITE.withAlpha(0),
                colorPickerScreen.getColor().withAlpha(255),
                RenderDirection.HORIZONTAL);

        double exactX = getX() + width * value;
        int handleX = (int) Math.round(exactX);

        RenderUtils.drawRectBorders(
                guiGraphics,
                CommonColors.DARK_GRAY,
                (float) (handleX - 1.0),
                (float) getY(),
                (float) (handleX + 1.0),
                (float) (getY() + getHeight()),
                1);

        if (this.isHovered) {
            guiGraphics.requestCursor(this.dragging ? CursorTypes.RESIZE_EW : CursorTypes.POINTING_HAND);
        }
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        this.dragging = this.active;
        updateValue(event.x());
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dragX, double dragY) {
        updateValue(event.x());
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
