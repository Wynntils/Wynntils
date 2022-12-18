/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.render.VerticalAlignment;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.objects.CustomColor;
import com.wynntils.mc.utils.ComponentUtils;
import com.wynntils.mc.utils.McUtils;
import com.wynntils.utils.StringUtils;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GuidesButton extends AbstractButton {
    private static final CustomColor BUTTON_COLOR = new CustomColor(181, 174, 151);
    private static final CustomColor BUTTON_COLOR_HOVERED = new CustomColor(121, 116, 101);

    private final Screen guideScreen;

    public GuidesButton(int x, int y, int width, int height, Screen guideScreen) {
        super(x, y, width, height, Component.literal("Guides Button"));
        this.guideScreen = guideScreen;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        CustomColor backgroundColor = this.isHovered ? BUTTON_COLOR_HOVERED : BUTTON_COLOR;
        RenderUtils.drawRect(poseStack, backgroundColor, this.x, this.y, 0, this.width, this.height);

        int maxTextWidth = this.width - 21;
        FontRenderer.getInstance()
                .renderText(
                        poseStack,
                        StringUtils.getMaxFittingText(
                                ComponentUtils.getUnformatted(guideScreen.getTitle()),
                                maxTextWidth,
                                FontRenderer.getInstance().getFont()),
                        this.x + 14,
                        this.y + 1,
                        0,
                        CommonColors.BLACK,
                        HorizontalAlignment.Left,
                        VerticalAlignment.Top,
                        FontRenderer.TextShadow.NONE);
    }

    @Override
    public void onPress() {
        McUtils.mc().setScreen(guideScreen);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}
}
