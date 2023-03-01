/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.type;

public enum PointerType {
    Arrow(10, 8, 0),
    Cursor(8, 7, 8),
    Narrow(8, 8, 15),
    Round(8, 8, 23),
    Straight(6, 8, 31),
    Triangle(8, 6, 39);
    public final int width;
    public final int height;
    public final int textureY;

    PointerType(int width, int height, int textureY) {
        this.width = width;
        this.height = height;
        this.textureY = textureY;
    }
}
