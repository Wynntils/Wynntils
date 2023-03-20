/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.type;

public enum ManaTexture {
    Wynn(0, 17, 8),
    Brune(83, 100, 8),
    Aether(116, 131, 7),
    Skull(143, 147, 8),
    Inverse(100, 115, 7),
    Skyrim(148, 163, 8),
    Rune(164, 179, 8),
    a(18, 33, 7),
    b(34, 51, 8),
    c(52, 67, 7),
    d(83, 100, 8);
    private final int textureY1, textureY2, height;

    ManaTexture(int textureY1, int textureY2, int height) {
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
