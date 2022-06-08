/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

import com.mojang.blaze3d.platform.Window;
import com.wynntils.mc.utils.McUtils;

public class OverlayPosition {
    private final float posX;
    private final float posY;

    private final int width;
    private final int height;

    private float drawingX;
    private float drawingY;

    public OverlayPosition(float posX, float posY, int width, int height) {
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
    }

    public float getX() {
        return posX;
    }

    public float getCurrentX() {
        return drawingX;
    }

    public float getY() {
        return posY;
    }

    public float getCurrentY() {
        return drawingY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void refresh() {
        Window screen = McUtils.mc().getWindow();

        drawingX = posX * screen.getWidth();
        drawingY = posY * screen.getHeight();
    }
}
