/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.gamebars;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import net.minecraft.client.renderer.MultiBufferSource;

public abstract class OverflowableBarOverlay extends BaseBarOverlay {
    protected OverflowableBarOverlay(OverlayPosition position, OverlaySize size, CustomColor textColor) {
        super(position, size, textColor);
    }

    @Override
    protected void renderBar(
            PoseStack poseStack, MultiBufferSource bufferSource, float renderY, float renderHeight, float progress) {
        int textureY1 = getTextureY1();
        int textureY2 = getTextureY2();

        Texture texture = getTexture();

        // Handle overflow
        if (Math.abs(progress) > 1) {
            Texture overflowTexture = getOverflowTexture();

            float x1 = this.getRenderX();
            float x2 = this.getRenderX() + this.getWidth();

            int half = (textureY1 + textureY2) / 2 + (textureY2 - textureY1) % 2;
            BufferedRenderUtils.drawProgressBarBackground(
                    poseStack, bufferSource, texture, x1, renderY, x2, renderY + renderHeight, 0, textureY1, 81, half);
            BufferedRenderUtils.drawProgressBarForeground(
                    poseStack,
                    bufferSource,
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
            BufferedRenderUtils.drawProgressBarForeground(
                    poseStack,
                    bufferSource,
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

        BufferedRenderUtils.drawProgressBar(
                poseStack,
                bufferSource,
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
