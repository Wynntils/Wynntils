/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.BasicTexturedButton;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class PresetButton extends BasicTexturedButton {
    private final float translationX;
    private final float translationY;
    private final StyledText message;

    public PresetButton(
            int x,
            int y,
            StyledText message,
            Consumer<Integer> onClick,
            List<Component> tooltip,
            float translationX,
            float translationY) {
        super(
                x,
                y,
                Texture.PAPER_BUTTON_RIGHT.width(),
                Texture.PAPER_BUTTON_RIGHT.height() / 2,
                Texture.PAPER_BUTTON_RIGHT,
                onClick,
                tooltip);

        this.message = message;
        this.translationX = translationX;
        this.translationY = translationY;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        // Only count as hovered if the mouse is outside of the background area as a slight bit
        // of the button is rendered underneath the background
        if (isHovered
                && MathUtils.isInside(
                        mouseX,
                        mouseY,
                        0,
                        Texture.ITEM_FILTER_BACKGROUND.width(),
                        0,
                        Texture.ITEM_FILTER_BACKGROUND.height())) {
            isHovered = false;
        }

        // When selected or hovered it should use the alternate texture
        RenderUtils.drawHoverableTexturedRect(poseStack, Texture.PAPER_BUTTON_RIGHT, getX(), getY(), this.isHovered);

        FontRenderer.getInstance()
                .renderScrollingAlignedTextInBox(
                        poseStack,
                        message,
                        getX() + 10,
                        getX() + getWidth() - 8,
                        getY() + 10,
                        getY() + getHeight() - 10,
                        getWidth() - 8,
                        translationX,
                        translationY,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);
    }
}
