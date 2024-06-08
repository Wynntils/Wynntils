/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.type;

public enum ObjectivesTextures implements BarTexture {
    WYNN(0, 9, 5),
    LIQUID(40, 49, 5),
    EMERALD(50, 59, 5),
    A(10, 19, 5),
    B(20, 29, 5),
    C(30, 39, 5);
    private final int textureY1, textureY2, height;

    ObjectivesTextures(int textureY1, int textureY2, int height) {
        this.textureY1 = textureY1;
        this.textureY2 = textureY2;
        this.height = height;
    }

    public int getTextureY1() {
        return textureY1;
    }

    public int getTextureY2() {
        return textureY2;
    }

    public int getHeight() {
        return height;
    }
}
