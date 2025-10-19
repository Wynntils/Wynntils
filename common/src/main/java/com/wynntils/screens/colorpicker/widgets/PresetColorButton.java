/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.colorpicker.widgets;

import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.screens.colorpicker.ColorPickerScreen;
import com.wynntils.utils.colors.CustomColor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class PresetColorButton extends WynntilsButton {
    private final ColorPickerScreen colorPickerScreen;
    private final CustomColor color;

    public PresetColorButton(
            int x, int y, int width, int height, CustomColor color, ColorPickerScreen colorPickerScreen) {
        super(x, y, width, height, Component.literal("Preset Color Widget"));

        this.color = color;
        this.colorPickerScreen = colorPickerScreen;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        //        PoseStack poseStack = guiGraphics.pose();

        //        RenderUtils.drawRect(poseStack, color, getX(), getY(), 0, width, height);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (!isMouseOver(event.x(), event.y())) return false;

        colorPickerScreen.setColor(color);

        return true;
    }

    @Override
    public void onPress(InputWithModifiers input) {}
}
