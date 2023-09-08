/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.RenderedStringUtils;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GuidesButton extends WynntilsButton {
    private static final CustomColor BUTTON_COLOR = new CustomColor(181, 174, 151);
    private static final CustomColor BUTTON_COLOR_HOVERED = new CustomColor(121, 116, 101);

    private final Screen guideScreen;

    public GuidesButton(int x, int y, int width, int height, Screen guideScreen) {
        super(x, y, width, height, Component.literal("Guides Button"));
        this.guideScreen = guideScreen;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        CustomColor backgroundColor = this.isHovered ? BUTTON_COLOR_HOVERED : BUTTON_COLOR;
        RenderUtils.drawRect(poseStack, backgroundColor, this.getX(), this.getY(), 0, this.width, this.height);

        int maxTextWidth = this.width - 21;
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StyledText.fromString(RenderedStringUtils.getMaxFittingText(
                                StyledText.fromComponent(guideScreen.getTitle()).getStringWithoutFormatting(),
                                maxTextWidth,
                                FontRenderer.getInstance().getFont())),
                        this.getX() + 14,
                        this.getY() + 1,
                        0,
                        CommonColors.BLACK,
                        HorizontalAlignment.LEFT,
                        VerticalAlignment.TOP,
                        TextShadow.NONE);
    }

    @Override
    public void onPress() {
        McUtils.mc().setScreen(guideScreen);
    }
}
