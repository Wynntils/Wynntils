/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.function.Consumer;
import net.minecraft.client.gui.Font;

public class ListSearchWidget extends SearchWidget {
    public ListSearchWidget(
            int x, int y, int width, int height, Consumer<String> onUpdateConsumer, TextboxScreen textboxScreen) {
        super(x, y, width, height, onUpdateConsumer, textboxScreen);
    }

    @Override
    protected void renderBackground(PoseStack poseStack) {
        RenderUtils.drawScalingTexturedRect(
                poseStack,
                Texture.LIST_SEARCH.resource(),
                this.getX(),
                this.getY(),
                0,
                this.width,
                this.height,
                Texture.LIST_SEARCH.width(),
                Texture.LIST_SEARCH.height());
    }

    @Override
    protected void renderText(
            PoseStack poseStack,
            String renderedText,
            int renderedTextStart,
            String firstPortion,
            String highlightedPortion,
            String lastPortion,
            Font font,
            int firstWidth,
            int highlightedWidth,
            int lastWidth,
            boolean defaultText) {
        poseStack.pushPose();

        poseStack.translate(getXOffset(), getYOffset(), 0);

        super.renderText(
                poseStack,
                renderedText,
                renderedTextStart,
                firstPortion,
                highlightedPortion,
                lastPortion,
                font,
                firstWidth,
                highlightedWidth,
                lastWidth,
                defaultText);

        poseStack.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX - getXOffset(), mouseY - getYOffset(), button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX - getXOffset(), mouseY - getYOffset(), button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(mouseX - getXOffset(), mouseY - getYOffset(), button, dragX, dragY);
    }

    private static int getYOffset() {
        return 5;
    }

    private static int getXOffset() {
        return 14;
    }
}
