/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features.overlays;

// TODO: This class is very basic atm
public class OverlayPosition {
    // Top-Left point of Overlay
    private int x;
    private int y;

    private OverlayPosition() {}

    public OverlayPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
