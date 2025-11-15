/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.wynntils.screens.colorpicker.ColorPickerScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ColorPickerWidget extends WynntilsButton {
    private static final Component TOOLTIP = Component.translatable("screens.wynntils.colorPicker.widgetTooltip");

    private final TextInputBoxWidget inputWidget;

    public ColorPickerWidget(int x, int y, int width, int height, TextInputBoxWidget inputWidget) {
        super(x, y, width, height, Component.literal("Color Picker Widget"));

        this.inputWidget = inputWidget;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawRectBorders(
                guiGraphics,
                isHovered ? CommonColors.LIGHT_GRAY : CommonColors.GRAY,
                getX(),
                getY(),
                getX() + getWidth(),
                getY() + getHeight(),
                2);
        CustomColor value = CustomColor.fromHexString(inputWidget.getTextBoxInput());
        RenderUtils.drawRect(guiGraphics, value, getX(), getY(), width, height);

        if (isHovered) {
            guiGraphics.setTooltipForNextFrame(TOOLTIP, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!isMouseOver(event.x(), event.y())) return false;

        McUtils.setScreen(ColorPickerScreen.create(McUtils.screen(), inputWidget));

        return true;
    }

    @Override
    public void onPress(InputWithModifiers input) {}
}
