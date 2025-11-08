/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BackButton extends WynntilsButton {
    private final Screen backTo;

    public BackButton(int x, int y, int width, int height, Screen backTo) {
        super(x, y, width, height, Component.literal("Back Button"));
        this.backTo = backTo;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        Texture backArrow = Texture.BACK_ARROW_OFFSET;
        if (this.isHovered) {
            RenderUtils.drawTexturedRect(
                    poseStack,
                    backArrow.resource(),
                    this.getX(),
                    this.getY(),
                    0,
                    this.width,
                    this.height,
                    backArrow.width() / 2,
                    0,
                    backArrow.width() / 2,
                    backArrow.height(),
                    backArrow.width(),
                    backArrow.height());
        } else {
            RenderUtils.drawTexturedRect(
                    poseStack,
                    backArrow.resource(),
                    this.getX(),
                    this.getY(),
                    0,
                    this.width,
                    this.height,
                    0,
                    0,
                    backArrow.width() / 2,
                    backArrow.height(),
                    backArrow.width(),
                    backArrow.height());
        }
    }

    @Override
    public void onPress() {
        McUtils.setScreen(backTo);
    }
}
