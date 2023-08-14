/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import net.minecraft.core.Position;
import org.joml.Vector2f;

public final class VectorUtils {
    public static Vector2f lineIntersection(Vector2f p0, Vector2f p1, Vector2f p2, Vector2f p3) {
        float epsilon = 0.001f;

        float a0 = p1.y() - p0.y();
        float b0 = p0.x() - p1.x();

        float a1 = p3.y() - p2.y();
        float b1 = p2.x() - p3.x();

        float det = a0 * b1 - a1 * b0;
        if (det > -epsilon && det < epsilon) {
            return null;
        } else {
            float c0 = a0 * p0.x + b0 * p0.y;
            float c1 = a1 * p2.x + b1 * p2.y;

            float x = (b1 * c0 - b0 * c1) / det;
            float y = (a0 * c1 - a1 * c0) / det;
            return new Vector2f(x, y);
        }
    }

    public static float distanceIgnoringY(Position pos1, Position pos2) {
        return (float) Math.sqrt(Math.pow(pos1.x() - pos2.x(), 2) + Math.pow(pos1.z() - pos2.z(), 2));
    }
}
