/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;

public abstract class Overlay {
    protected OverlayPosition position;

    protected float width;
    protected float height;

    public Overlay(OverlayPosition position, float width, float height) {
        this.position = position;
        this.width = width;
        this.height = height;
    }

    public abstract void render(
            OverlayPosition overlayPosition, PoseStack poseStack, float partialTicks, Window window);

    public OverlayPosition getPosition() {
        return position;
    }

    public void setPosition(OverlayPosition position) {
        this.position = position;
    }

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }
}
