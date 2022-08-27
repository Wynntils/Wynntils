/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.user.overlays.map;

public enum PointerType {
    ARROW(10, 8, 0),
    CURSOR(8, 7, 8),
    NARROW(8, 8, 15),
    ROUND(8, 8, 23),
    STRAIGHT(6, 8, 31),
    TRIANGLE(8, 6, 39);
    public int width, height, textureY;

    PointerType(int width, int height, int textureY) {
        this.width = width;
        this.height = height;
        this.textureY = textureY;
    }
}
