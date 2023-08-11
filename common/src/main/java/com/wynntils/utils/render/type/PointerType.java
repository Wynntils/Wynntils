/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.type;

public enum PointerType {
    ARROW(10, 8, 0),
    CURSOR(8, 7, 8),
    NARROW(8, 8, 15),
    ROUND(8, 8, 23),
    STRAIGHT(6, 8, 31),
    TRIANGLE(8, 6, 39);
    public final int width;
    public final int height;
    public final int textureY;

    PointerType(int width, int height, int textureY) {
        this.width = width;
        this.height = height;
        this.textureY = textureY;
    }
}
