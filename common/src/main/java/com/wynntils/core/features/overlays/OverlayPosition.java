/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.mc.utils.McUtils;

public class OverlayPosition { // Should be changed to RenderPosition if Overlays merge with Features
    // Position along the x and y-axis as a percentage of the total screen width
    public float x;
    public float y;

    public int width;
    public int height;

    // A cached value for the x and y-axis position according to the current screen size
    private float drawingX;
    private float drawingY;

    public OverlayPosition(float x, float y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public float getCurrentX() {
        this.refresh();

        return drawingX;
    }

    public float getCurrentY() {
        this.refresh();

        return drawingY;
    }

    public void refresh() {
        Window screen = McUtils.mc().getWindow();

        drawingX = x * screen.getWidth();
        drawingY = y * screen.getHeight();
    }
}
