/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.type;

public enum ObjectivesTextures {
    Wynn(0),
    Liquid(40),
    Emerald(50),
    a(10),
    b(20),
    c(30);

    private final int yOffset;

    ObjectivesTextures(int yOffset) {
        this.yOffset = yOffset;
    }

    public int getTextureYOffset() {
        return yOffset;
    }
}
