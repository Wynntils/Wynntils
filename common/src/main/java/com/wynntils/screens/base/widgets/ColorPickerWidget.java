/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.colorpicker.ColorPickerScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class ColorPickerWidget extends WynntilsButton {
    private final TextInputBoxWidget inputWidget;

    public ColorPickerWidget(int x, int y, int width, int height, TextInputBoxWidget inputWidget) {
        super(x, y, width, height, Component.literal("Color Picker Widget"));

        this.inputWidget = inputWidget;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        RenderUtils.drawRectBorders(
                poseStack, CommonColors.GRAY, getX(), getY(), getX() + getWidth(), getY() + getHeight(), 1, 2);
        CustomColor value = CustomColor.fromHexString(inputWidget.getTextBoxInput());
        RenderUtils.drawRect(poseStack, value, getX(), getY(), 0, width, height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        McUtils.mc().setScreen(ColorPickerScreen.create(McUtils.mc().screen, inputWidget));

        return true;
    }

    @Override
    public void onPress() {}
}
