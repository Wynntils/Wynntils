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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;

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
            GuiGraphics guiGraphics,
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
        //        poseStack.pushPose();

        //        poseStack.translate(getXOffset(), getYOffset(), 0);

        super.renderText(
                guiGraphics,
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

        //        poseStack.popPose();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        return super.mouseClicked(
                new MouseButtonEvent(event.x() - getXOffset(), event.y() - getYOffset(), event.buttonInfo()),
                isDoubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return super.mouseReleased(
                new MouseButtonEvent(event.x() - getXOffset(), event.y() - getYOffset(), event.buttonInfo()));
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return super.mouseDragged(
                new MouseButtonEvent(event.x() - getXOffset(), event.y() - getYOffset(), event.buttonInfo()),
                dragX,
                dragY);
    }

    private static int getYOffset() {
        return 5;
    }

    private static int getXOffset() {
        return 14;
    }
}
