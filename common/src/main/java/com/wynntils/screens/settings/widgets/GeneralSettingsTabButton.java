/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.settings.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.base.widgets.BasicTexturedButton;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public abstract class GeneralSettingsTabButton extends BasicTexturedButton {
    private static final int MAX_OFFSET = -8;
    private static final int MIN_OFFSET = 0;

    protected boolean selectedTab = false;
    protected Texture tagTexture;
    protected Texture iconTexture;

    private final OffsetDirection offsetDirection;
    private final int offsetX;
    private final int offsetY;

    private int hoverOffset = 0;

    GeneralSettingsTabButton(
            int x,
            int y,
            int width,
            int height,
            Consumer<Integer> onClick,
            List<Component> tooltip,
            Texture tagTexture,
            Texture iconTexture,
            OffsetDirection offsetDirection,
            int offsetX,
            int offsetY) {
        super(x, y, width, height, tagTexture, onClick, tooltip);
        this.tagTexture = tagTexture;
        this.iconTexture = iconTexture;
        this.offsetDirection = offsetDirection;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();

        // Don't count as hovered if mouse is hovering the book as the tags render
        // slightly underneath the book
        if (isHovered
                && ((offsetDirection == OffsetDirection.UP && mouseY >= offsetY)
                        || (offsetDirection == OffsetDirection.RIGHT
                                && mouseX <= offsetX + Texture.CONFIG_BOOK_BACKGROUND.width())
                        || (offsetDirection == OffsetDirection.DOWN
                                && mouseY <= offsetY + Texture.CONFIG_BOOK_BACKGROUND.height())
                        || (offsetDirection == OffsetDirection.LEFT && mouseX >= offsetX))) {
            isHovered = false;
        }

        // Determine the offset of the tag. When selected the max offset should be used, otherwise when hovered
        // increase until limit reached.
        if (selectedTab) {
            this.hoverOffset = (offsetDirection == OffsetDirection.UP || offsetDirection == OffsetDirection.LEFT)
                    ? MAX_OFFSET
                    : -MAX_OFFSET;
        } else if (this.isHovered) {
            hoverOffset += (offsetDirection == OffsetDirection.UP || offsetDirection == OffsetDirection.LEFT) ? -1 : 1;
            hoverOffset = (offsetDirection == OffsetDirection.UP || offsetDirection == OffsetDirection.LEFT)
                    ? Math.max(hoverOffset, MAX_OFFSET)
                    : Math.min(hoverOffset, -MAX_OFFSET);
        } else {
            hoverOffset += (offsetDirection == OffsetDirection.UP || offsetDirection == OffsetDirection.LEFT) ? 1 : -1;
            hoverOffset = (offsetDirection == OffsetDirection.UP || offsetDirection == OffsetDirection.LEFT)
                    ? Math.min(hoverOffset, MIN_OFFSET)
                    : Math.max(hoverOffset, MIN_OFFSET);
        }

        int xOffset = 0;
        int yOffset = 0;

        // Move the tag render position
        if (offsetDirection == OffsetDirection.RIGHT || offsetDirection == OffsetDirection.LEFT) {
            xOffset = hoverOffset;
        } else {
            yOffset = hoverOffset;
        }

        RenderUtils.drawTexturedRect(poseStack, tagTexture, this.getX() + xOffset, this.getY() + yOffset);

        // Render icon on tag
        if (offsetDirection == OffsetDirection.UP || offsetDirection == OffsetDirection.DOWN) {
            RenderUtils.drawTexturedRect(
                    poseStack,
                    iconTexture,
                    getX() + (getWidth() - iconTexture.width()) / 2f + xOffset,
                    getY() + 14 + yOffset);
        } else {
            RenderUtils.drawTexturedRect(
                    poseStack,
                    iconTexture,
                    getX() + 14 + xOffset,
                    getY() + (getHeight() - iconTexture.height()) / 2f + yOffset);
        }
    }

    protected enum OffsetDirection {
        UP,
        RIGHT,
        DOWN,
        LEFT
    }
}
