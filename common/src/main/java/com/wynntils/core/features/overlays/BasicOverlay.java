/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.utils.objects.PentaConsumer;

public class BasicOverlay extends Overlay {
    private final PentaConsumer<Overlay, OverlayPosition, PoseStack, Float, Window> renderConsumer;

    public BasicOverlay(
            OverlayPosition position,
            float width,
            float height,
            PentaConsumer<Overlay, OverlayPosition, PoseStack, Float, Window> renderConsumer) {
        super(position, width, height);
        this.renderConsumer = renderConsumer;
    }

    @Override
    public void render(OverlayPosition overlayPosition, PoseStack poseStack, float partialTicks, Window window) {
        renderConsumer.consume(this, overlayPosition, poseStack, partialTicks, window);
    }
}
