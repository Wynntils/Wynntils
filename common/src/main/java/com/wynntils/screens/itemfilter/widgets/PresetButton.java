/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.itemfilter.widgets;

import com.wynntils.core.text.StyledText;
import com.wynntils.screens.base.widgets.BasicTexturedButton;
import com.wynntils.utils.MathUtils;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.RenderDirection;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class PresetButton extends BasicTexturedButton {
    private final int offsetX;
    private final int offsetY;
    private final StyledText message;

    public PresetButton(
            int x,
            int y,
            StyledText message,
            Consumer<Integer> onClick,
            List<Component> tooltip,
            int offsetX,
            int offsetY) {
        super(
                x,
                y,
                Texture.BUTTON_RIGHT.width(),
                Texture.BUTTON_RIGHT.height() / 2,
                Texture.BUTTON_RIGHT,
                onClick,
                tooltip);

        this.message = message;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Only count as hovered if the mouse is outside of the background area as a slight bit
        // of the button is rendered underneath the background
        if (isHovered
                && MathUtils.isInside(
                        mouseX,
                        mouseY,
                        offsetX,
                        Texture.ITEM_FILTER_BACKGROUND.width(),
                        offsetY,
                        Texture.ITEM_FILTER_BACKGROUND.height())) {
            isHovered = false;
        }

        // When selected or hovered it should use the alternate texture
        RenderUtils.drawHoverableTexturedRect(
                guiGraphics, Texture.BUTTON_RIGHT, getX(), getY(), this.isHovered, RenderDirection.VERTICAL);

        FontRenderer.getInstance()
                .renderScrollingAlignedTextInBox(
                        guiGraphics,
                        message,
                        getX() + 10,
                        getX() + getWidth() - 8,
                        getY() + 10,
                        getY() + getHeight() - 10,
                        getWidth() - 8,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);
    }
}
