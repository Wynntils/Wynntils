/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.guides.widgets;

import com.wynntils.screens.base.widgets.WynntilsButton;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public abstract class GuideNavigationButton extends WynntilsButton {
    private final ItemStack itemToRender;

    protected GuideNavigationButton(int x, int y, ItemStack itemToRender) {
        super(x, y, 20, 20, Component.empty());
        this.itemToRender = itemToRender;
    }

    @Override
    public void renderContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderUtils.drawNineSliceScalingTexturedRect(
                guiGraphics,
                isHovered ? Texture.GUIDE_WIDGET_BACKGROUND_HOVERED : Texture.GUIDE_WIDGET_BACKGROUND,
                getX(),
                getY(),
                getWidth(),
                getHeight());

        RenderUtils.renderItem(guiGraphics, itemToRender, getX() + 2, getY() + 2);

        if (isHovered) {
            guiGraphics.setTooltipForNextFrame(getTooltip(), mouseX, mouseY);
        }
    }

    protected abstract Component getTooltip();
}
