/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;

public abstract class Overlay {
    // Top-Left point of Overlay
    private int x;
    private int y;

    private int width;
    private int height;

    public abstract void render(PoseStack poseStack, float partialTicks, Window window);
}
