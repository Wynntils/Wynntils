/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render.type;

import org.joml.Vector2f;

public record CircleMask(float centerX, float centerY, float radiusX, float radiusY) {
    public static CircleMask fromBounds(float x, float y, float width, float height) {
        return new CircleMask(x + width / 2f, y + height / 2f, width / 2f, height / 2f);
    }

    public Vector2f point(float angle) {
        return new Vector2f(centerX + (float) Math.cos(angle) * radiusX, centerY + (float) Math.sin(angle) * radiusY);
    }
}
