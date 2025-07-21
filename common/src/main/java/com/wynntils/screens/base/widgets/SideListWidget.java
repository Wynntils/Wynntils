/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public abstract class SideListWidget extends AbstractWidget {
    protected final boolean root;
    protected final String name;

    protected SideListWidget(int y, int width, int height, String name, boolean root) {
        super(0, y, width, height, Component.literal("Side List Widget"));

        this.name = name;
        this.root = root;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();
        renderBackground(poseStack);

        if (root) {
            FontRenderer.getInstance()
                    .renderText(
                            poseStack,
                            StyledText.fromString("⏎"),
                            getX() + 10,
                            getY() + getHeight() / 2f,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);

            FontRenderer.getInstance()
                    .renderScrollingText(
                            poseStack,
                            StyledText.fromString(name),
                            getX() + 20,
                            getY() + getHeight() / 2f,
                            getWidth() - 30,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL);
        } else {
            FontRenderer.getInstance()
                    .renderScrollingText(
                            poseStack,
                            StyledText.fromString(name),
                            getX() + 10,
                            getY() + getHeight() / 2f,
                            getWidth() - 20,
                            CommonColors.WHITE,
                            HorizontalAlignment.LEFT,
                            VerticalAlignment.MIDDLE,
                            TextShadow.NORMAL,
                            1);
        }
    }

    protected void renderBackground(PoseStack poseStack) {
        RenderUtils.drawRect(
                poseStack,
                CommonColors.BROWN.withAlpha(isHovered ? 150 : 100),
                this.getX(),
                this.getY(),
                0,
                width,
                height);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
