/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.features.overlays.sizes.FixedOverlaySize;
import com.wynntils.core.features.overlays.sizes.OverlaySize;
import com.wynntils.mc.render.HorizontalAlignment;
import com.wynntils.mc.render.VerticalAlignment;

public abstract class Overlay {
    protected OverlayPosition position;

    protected OverlaySize size;

    // This is used in rendering.
    // Initially we use the overlay position horizontal alignment
    // but the user can modify this config field to use an override.
    // Example use case: Overlay is aligned to the left in the TopRight section,
    //                   but the user wants to use right text alignment
    protected HorizontalAlignment horizontalAlignmentOverride = null;
    protected VerticalAlignment verticalAlignmentOverride = null;

    public Overlay(OverlayPosition position, float width, float height) {
        this.position = position;
        this.size = new FixedOverlaySize(width, height);
    }

    public Overlay(OverlayPosition position, OverlaySize size) {
        this.position = position;
        this.size = size;
    }

    public Overlay(
            OverlayPosition position,
            OverlaySize size,
            HorizontalAlignment horizontalAlignmentOverride,
            VerticalAlignment verticalAlignmentOverride) {
        this.position = position;
        this.size = size;
        this.horizontalAlignmentOverride = horizontalAlignmentOverride;
        this.verticalAlignmentOverride = verticalAlignmentOverride;
    }

    public abstract void render(PoseStack poseStack, float partialTicks, Window window);

    public float getWidth() {
        return this.size.getWidth();
    }

    public float getHeight() {
        return this.size.getHeight();
    }

    public float getRenderedWidth() {
        return this.size.getRenderedWidth();
    }

    public float getRenderedHeight() {
        return this.size.getRenderedHeight();
    }

    // Return the X where the overlay should be rendered
    public int getRenderX() {
        final SectionCoordinates section = OverlayManager.getSection(this.position.getAnchorSection());
        return switch (this.position.getHorizontalAlignment()) {
            case Left -> section.x1() + this.position.getHorizontalOffset();
            case Center -> (int) (section.x1() + section.x2() - this.getWidth()) / 2
                    + this.position.getHorizontalOffset();
            case Right -> (int) (section.x2() + this.position.getHorizontalOffset() - this.getWidth());
        };
    }

    // Return the Y where the overlay should be rendered
    public int getRenderY() {
        final SectionCoordinates section = OverlayManager.getSection(this.position.getAnchorSection());
        return switch (this.position.getVerticalAlignment()) {
            case Top -> section.y1() + this.position.getVerticalOffset();
            case Middle -> (int) (section.y1() + section.y2() - this.getHeight()) / 2
                    + this.position.getVerticalOffset();
            case Bottom -> (int) (section.y2() + this.position.getVerticalOffset() - this.getHeight());
        };
    }

    public HorizontalAlignment getRenderHorizontalAlignment() {
        return horizontalAlignmentOverride == null ? position.getHorizontalAlignment() : horizontalAlignmentOverride;
    }

    public VerticalAlignment getRenderVerticalAlignment() {
        return verticalAlignmentOverride == null ? position.getVerticalAlignment() : verticalAlignmentOverride;
    }
}
