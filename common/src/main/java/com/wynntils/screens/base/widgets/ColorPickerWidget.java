/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.colorpicker.ColorPickerScreen;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class ColorPickerWidget extends WynntilsButton {
    private static final List<Component> TOOLTIP =
            List.of(Component.translatable("screens.wynntils.colorPicker.widgetTooltip"));

    private final TextInputBoxWidget inputWidget;

    public ColorPickerWidget(int x, int y, int width, int height, TextInputBoxWidget inputWidget) {
        super(x, y, width, height, Component.literal("Color Picker Widget"));

        this.inputWidget = inputWidget;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        RenderUtils.drawRectBorders(
                poseStack,
                isHovered ? CommonColors.LIGHT_GRAY : CommonColors.GRAY,
                getX(),
                getY(),
                getX() + getWidth(),
                getY() + getHeight(),
                1,
                2);
        CustomColor value = CustomColor.fromHexString(inputWidget.getTextBoxInput());
        RenderUtils.drawRect(poseStack, value, getX(), getY(), 0, width, height);

        if (isHovered) {
            McUtils.screen().setTooltipForNextRenderPass(Lists.transform(TOOLTIP, Component::getVisualOrderText));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY)) return false;

        McUtils.setScreen(ColorPickerScreen.create(McUtils.screen(), inputWidget));

        return true;
    }

    @Override
    public void onPress() {}
}
