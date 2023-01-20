/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.config.ConfigHolder;
import com.wynntils.core.features.overlays.sizes.OverlaySize;
import com.wynntils.utils.type.QuadConsumer;
import net.minecraft.client.renderer.MultiBufferSource;

public class BasicOverlay extends Overlay {
    private final QuadConsumer<Overlay, PoseStack, Float, Window> renderConsumer;
    private final QuadConsumer<Overlay, PoseStack, Float, Window> renderPreviewConsumer;

    public BasicOverlay(
            OverlayPosition position,
            OverlaySize overlaySize,
            QuadConsumer<Overlay, PoseStack, Float, Window> renderConsumer,
            QuadConsumer<Overlay, PoseStack, Float, Window> renderPreviewConsumer) {
        super(position, overlaySize);
        this.renderConsumer = renderConsumer;
        this.renderPreviewConsumer = renderPreviewConsumer;
    }

    public BasicOverlay(
            OverlayPosition position,
            OverlaySize overlaySize,
            QuadConsumer<Overlay, PoseStack, Float, Window> renderConsumer) {
        super(position, overlaySize);
        this.renderConsumer = renderConsumer;
        this.renderPreviewConsumer = renderConsumer;
    }

    @Override
    public void render(
            PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTicks, Window window) {
        renderConsumer.consume(this, poseStack, partialTicks, window);
    }

    @Override
    public void renderPreview(
            PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTicks, Window window) {
        renderPreviewConsumer.consume(this, poseStack, partialTicks, window);
    }

    @Override
    protected void onConfigUpdate(ConfigHolder configHolder) {}
}
