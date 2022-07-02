/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.utils.objects.Pair;

public abstract class Overlay {
    protected OverlayPosition position;

    protected float width;
    protected float height;

    public Overlay(OverlayPosition position, float width, float height) {
        this.position = position;
        this.width = width;
        this.height = height;
    }

    public abstract void render(PoseStack poseStack, float partialTicks, Window window);

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }

    // Return the X where the overlay should be rendered
    public int getRenderX() {
        final Pair<Coordinate, Coordinate> ninth = OverlayManager.getNinth(this.position.getAnchorNinth());
        return switch (this.position.getHorizontalAlignment()) {
            case Left -> ninth.a.x() + this.position.getHorizontalOffset();
            case Center -> (int) (ninth.a.x() + ninth.b.x() - this.width) / 2 + this.position.getHorizontalOffset();
            case Right -> (int) (ninth.b.x() - this.position.getHorizontalOffset() - this.width);
        };
    }

    // Return the Y where the overlay should be rendered
    public int getRenderY() {
        final Pair<Coordinate, Coordinate> ninth = OverlayManager.getNinth(this.position.getAnchorNinth());
        return switch (this.position.getVerticalAlignment()) {
            case Top -> ninth.a.y() + this.position.getVerticalOffset();
            case Middle -> (int) (ninth.a.y() + ninth.b.y() - this.height) / 2 + this.position.getVerticalOffset();
            case Bottom -> (int) (ninth.b.y() - this.position.getVerticalOffset() - this.height);
        };
    }
}
