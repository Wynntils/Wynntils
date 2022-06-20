/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;

public abstract class OverlayBase {
    protected int width;
    protected int height;

    public abstract void render(
            OverlayPosition overlayPosition, PoseStack poseStack, float partialTicks, Window window);
}
