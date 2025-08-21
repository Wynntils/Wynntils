/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.gamebars;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.handlers.bossbar.TrackedBar;
import com.wynntils.handlers.bossbar.type.BossBarProgress;
import com.wynntils.models.abilities.bossbars.ManaBankBar;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.render.buffered.BufferedRenderUtils;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.renderer.MultiBufferSource;

public class ManaBankBarOverlay extends ManaBarOverlay {
    public ManaBankBarOverlay() {
        super(
                new OverlayPosition(
                        -30,
                        -150,
                        VerticalAlignment.BOTTOM,
                        HorizontalAlignment.CENTER,
                        OverlayPosition.AnchorSection.BOTTOM_MIDDLE),
                new OverlaySize(81, 21));
    }

    @Override
    public String icon() {
        return "☄";
    }

    @Override
    public BossBarProgress progress() {
        return Models.Ability.manaBankBar.getBarProgress();
    }

    @Override
    protected Class<? extends TrackedBar> getTrackedBarClass() {
        return ManaBankBar.class;
    }

    @Override
    public boolean isVisible() {
        return Models.Ability.manaBankBar.isActive();
    }

    @Override
    protected void renderBar(
            PoseStack poseStack, MultiBufferSource bufferSource, float renderY, float renderHeight, float progress) {
        int textureY1 = getTextureY1();
        int textureY2 = getTextureY2();

        Texture texture = getTexture();

        float x1 = this.getRenderX();
        float x2 = this.getRenderX() + this.getWidth();

        int half = (textureY1 + textureY2) / 2 + (textureY2 - textureY1) % 2;
        BufferedRenderUtils.drawProgressBarBackground(
                poseStack, bufferSource, texture, x1, renderY, x2, renderY + renderHeight, 0, textureY1, 81, half);
        if (progress == 1f) {
            BufferedRenderUtils.drawProgressBarForeground(
                    poseStack,
                    bufferSource,
                    getOverflowTexture(),
                    x1,
                    renderY,
                    x2,
                    renderY + renderHeight,
                    0,
                    half,
                    81,
                    textureY2 + (textureY2 - textureY1) % 2,
                    1f);
        } else {
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
                    progress);
        }
    }

    @Override
    protected void onConfigUpdate(Config<?> config) {
        // Do not call super
    }
}
