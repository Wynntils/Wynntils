/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.base.widgets;

import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class BasicHoverableButton extends BasicTexturedButton {
    private final Texture hoverTexture;

    public BasicHoverableButton(
            int x,
            int y,
            int width,
            int height,
            Texture texture,
            Texture hoverTexture,
            Consumer<Integer> onClick,
            List<Component> tooltip) {
        super(x, y, width, height, texture, onClick, tooltip, true);
        this.hoverTexture = hoverTexture;
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (scaleTexture) {
            RenderUtils.drawScalingTexturedRect(
                    guiGraphics,
                    isHovered ? hoverTexture.identifier() : texture.identifier(),
                    this.getX(),
                    this.getY(),
                    getWidth(),
                    getHeight(),
                    texture.width(),
                    texture.height());
        } else {
            RenderUtils.drawTexturedRect(guiGraphics, isHovered ? hoverTexture : texture, this.getX(), this.getY());
        }
    }
}
