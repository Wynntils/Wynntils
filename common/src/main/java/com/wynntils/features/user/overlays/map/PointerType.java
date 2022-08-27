/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.overlays.map;

public enum PointerType {
    Arrow(10, 8, 0),
    Cursor(8, 7, 8),
    Narrow(8, 8, 15),
    Round(8, 8, 23),
    Straight(6, 8, 31),
    Triangle(8, 6, 39);
    public int width, height, textureY;

    PointerType(int width, int height, int textureY) {
        this.width = width;
        this.height = height;
        this.textureY = textureY;
    }
}
