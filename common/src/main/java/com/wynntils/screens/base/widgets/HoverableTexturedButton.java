/*
 * Copyright © Wynntils 2024-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.wynntils.core.text.StyledText;
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

public class HoverableTexturedButton extends BasicTexturedButton {
    private final StyledText message;
    private final Texture buttonTexture;
    private final Texture backgroundTexture;
    private final int offsetX;
    private final int offsetY;

    private boolean isSelected;

    public HoverableTexturedButton(
            int x,
            int y,
            int width,
            int height,
            StyledText message,
            Consumer<Integer> onClick,
            List<Component> tooltip,
            Texture buttonTexture,
            Texture backgroundTexture,
            boolean isSelected,
            int offsetX,
            int offsetY) {
        super(x, y, width, height, buttonTexture, onClick, tooltip);

        this.message = message;
        this.buttonTexture = buttonTexture;
        this.backgroundTexture = backgroundTexture;
        this.isSelected = isSelected;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Only count as hovered if the mouse is outside of the background area as a slight bit
        // of the button is often rendered underneath the background
        if (isHovered
                && MathUtils.isInside(
                        mouseX,
                        mouseY,
                        offsetX,
                        offsetX + backgroundTexture.width(),
                        offsetY,
                        offsetY + backgroundTexture.height())) {
            isHovered = false;
        }

        // When selected or hovered it should use the alternate texture
        RenderUtils.drawHoverableTexturedRect(
                guiGraphics,
                buttonTexture,
                getX(),
                getY(),
                this.isHovered || this.isSelected,
                RenderDirection.VERTICAL);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics,
                        message,
                        getX() + 2,
                        getX() + getWidth() - 4,
                        getY() + 10,
                        getY() + getHeight() - 10,
                        getWidth() - 4,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.TOP,
                        TextShadow.NORMAL);
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}
