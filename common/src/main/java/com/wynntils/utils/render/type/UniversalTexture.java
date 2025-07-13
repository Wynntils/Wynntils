/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.type;

public enum UniversalTexture implements BarTexture {
    A(0, 16, 8),
    B(16, 32, 8),
    C(32, 48, 8),
    D(48, 66, 9),
    E(66, 84, 9),
    F(84, 100, 8),
    G(100, 116, 8),
    H(116, 132, 8),
    HEARTS(132, 150, 9),
    MANA_ORBS(150, 168, 9),
    GRUNE(168, 184, 8),
    AETHER(184, 198, 7),
    SKYRIM(198, 214, 8),
    LEFT_SKULL(214, 230, 8),
    RIGHT_SKULL(230, 246, 8),
    RUNE_A(246, 262, 8),
    RUNE_B(262, 278, 8),
    EXPERIENCE_A(278, 288, 5),
    EXPERIENCE_B(288, 298, 5),
    EXPERIENCE_C(298, 308, 5),
    EXPERIENCE_D(308, 318, 5),
    EXPERIENCE_LIQUID(318, 328, 5),
    EXPERIENCE_EMERALD(328, 338, 5);

    private final int textureY1;
    private final int textureY2;
    private final int height;

    UniversalTexture(int textureY1, int textureY2, int height) {
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
