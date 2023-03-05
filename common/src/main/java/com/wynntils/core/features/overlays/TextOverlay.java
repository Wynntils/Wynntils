/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.overlays.sizes.OverlaySize;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * An overlay, which main purpose is to display function templates.
 */
public abstract class TextOverlay extends Overlay {
    @Config
    public TextShadow textShadow = TextShadow.OUTLINE;

    @Config
    public String content = "";

    @Config
    public float secondsPerRecalculation = 0.5f;

    protected String[] cachedLines;
    protected long lastUpdate = 0;

    public TextOverlay(OverlayPosition position, float width, float height) {
        super(position, width, height);
    }

    public TextOverlay(OverlayPosition position, OverlaySize size) {
        super(position, size);
    }

    public TextOverlay(
            OverlayPosition position,
            OverlaySize size,
            HorizontalAlignment horizontalAlignmentOverride,
            VerticalAlignment verticalAlignmentOverride) {
        super(position, size, horizontalAlignmentOverride, verticalAlignmentOverride);
    }

    @Override
    public void render(
            PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTicks, Window window) {
        if (!Models.WorldState.onWorld()) return;

        if (System.nanoTime() - lastUpdate > secondsPerRecalculation * 1e+9) {
            lastUpdate = System.nanoTime();
            cachedLines = Managers.Function.doFormatLines(content);
        }

        float renderX = this.getRenderX();
        float renderY = this.getRenderY();
        for (String line : cachedLines) {
            BufferedFontRenderer.getInstance()
                    .renderAlignedTextInBox(
                            poseStack,
                            bufferSource,
                            line,
                            renderX,
                            renderX + this.getWidth(),
                            renderY,
                            renderY + this.getHeight(),
                            0,
                            CommonColors.WHITE,
                            this.getRenderHorizontalAlignment(),
                            this.getRenderVerticalAlignment(),
                            this.textShadow);

            renderY += FontRenderer.getInstance().getFont().lineHeight;
        }
    }

    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {}
}
