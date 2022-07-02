/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.features.overlays.overlaySizes.FixedOverlaySize;
import com.wynntils.core.features.overlays.overlaySizes.OverlaySize;
import com.wynntils.utils.objects.Pair;

public abstract class Overlay {
    protected OverlayPosition overlayPosition;

    protected OverlaySize overlaySize;

    public Overlay(OverlayPosition overlayPosition, float width, float height) {
        this.overlayPosition = overlayPosition;
        this.overlaySize = new FixedOverlaySize(width, height);
    }

    public Overlay(OverlayPosition overlayPosition, OverlaySize overlaySize) {
        this.overlayPosition = overlayPosition;
        this.overlaySize = overlaySize;
    }

    public abstract void render(PoseStack poseStack, float partialTicks, Window window);

    public float getWidth() {
        return this.overlaySize.getWidth();
    }

    public float getHeight() {
        return this.overlaySize.getHeight();
    }

    // Return the X where the overlay should be rendered
    public int getRenderX() {
        final Pair<Coordinate, Coordinate> ninth = OverlayManager.getNinth(this.overlayPosition.getAnchorNinth());
        return switch (this.overlayPosition.getHorizontalAlignment()) {
            case Left -> ninth.a.x() + this.overlayPosition.getHorizontalOffset();
            case Center -> (int) (ninth.a.x() + ninth.b.x() - this.getWidth()) / 2
                    + this.overlayPosition.getHorizontalOffset();
            case Right -> (int) (ninth.b.x() - this.overlayPosition.getHorizontalOffset() - this.getWidth());
        };
    }

    // Return the Y where the overlay should be rendered
    public int getRenderY() {
        final Pair<Coordinate, Coordinate> ninth = OverlayManager.getNinth(this.overlayPosition.getAnchorNinth());
        return switch (this.overlayPosition.getVerticalAlignment()) {
            case Top -> ninth.a.y() + this.overlayPosition.getVerticalOffset();
            case Middle -> (int) (ninth.a.y() + ninth.b.y() - this.getHeight()) / 2
                    + this.overlayPosition.getVerticalOffset();
            case Bottom -> (int) (ninth.b.y() - this.overlayPosition.getVerticalOffset() - this.getHeight());
        };
    }
}
