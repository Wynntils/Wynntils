/*
 * Copyright © Wynntils 2022-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.gamebars;

import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.RenderUtils;
import com.wynntils.utils.render.Texture;
import net.minecraft.client.gui.GuiGraphics;

public abstract class OverflowableBarOverlay extends BaseBarOverlay {
    protected OverflowableBarOverlay(OverlayPosition position, OverlaySize size, CustomColor textColor) {
        super(position, size, textColor);
    }

    @Override
    protected void renderBar(GuiGraphics guiGraphics, float renderY, float renderHeight, float progress) {
        int textureY1 = getTextureY1();
        int textureY2 = getTextureY2();

        Texture texture = getTexture();

        // Handle overflow
        if (Math.abs(progress) > 1) {
            Texture overflowTexture = getOverflowTexture();

            float x1 = this.getRenderX();
            float x2 = this.getRenderX() + this.getWidth();

            int half = (textureY1 + textureY2) / 2 + (textureY2 - textureY1) % 2;
            RenderUtils.drawProgressBarBackground(
                    guiGraphics, texture, x1, renderY, x2, renderY + renderHeight, 0, textureY1, 81, half);
            RenderUtils.drawProgressBarForeground(
                    guiGraphics,
                    texture,
                    x1,
                    renderY,
                    x2,
                    renderY + renderHeight,
                    0,
                    half,
                    81,
                    textureY2 + (textureY2 - textureY1) % 2,
                    1f);

            float overflowProgress = progress < 0 ? progress + 1 : progress - 1;
            RenderUtils.drawProgressBarForeground(
                    guiGraphics,
                    overflowTexture,
                    x1,
                    renderY,
                    x2,
                    renderY + renderHeight,
                    0,
                    half,
                    81,
                    textureY2 + (textureY2 - textureY1) % 2,
                    overflowProgress);

            return;
        }

        RenderUtils.drawProgressBar(
                guiGraphics,
                texture,
                this.getRenderX(),
                renderY,
                this.getRenderX() + this.getWidth(),
                renderY + renderHeight,
                0,
                textureY1,
                81,
                textureY2,
                progress);
    }

    protected abstract Texture getTexture();

    protected abstract Texture getOverflowTexture();

    protected abstract int getTextureY1();

    protected abstract int getTextureY2();
}
