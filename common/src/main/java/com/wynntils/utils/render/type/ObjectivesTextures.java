/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.type;

public enum ObjectivesTextures {
    WYNN(0),
    LIQUID(40),
    EMERALD(50),
    A(10),
    B(20),
    C(30);

    private final int yOffset;

    ObjectivesTextures(int yOffset) {
        this.yOffset = yOffset;
    }

    public int getTextureYOffset() {
        return yOffset;
    }
}
