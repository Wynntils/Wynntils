/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.type;

public enum HealthTexture {
    Wynn(0, 17, 8),
    Grune(84, 99, 7),
    Aether(100, 115, 7),
    Skull(116, 131, 8),
    Skyrim(132, 147, 8),
    Rune(148, 163, 8),
    a(18, 33, 7),
    b(34, 51, 8),
    c(52, 67, 7),
    d(68, 83, 7);
    private final int textureY1, textureY2, height;

    HealthTexture(int textureY1, int textureY2, int height) {
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
