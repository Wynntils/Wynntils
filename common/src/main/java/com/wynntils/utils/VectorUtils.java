/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import org.joml.Vector3f;

public final class VectorUtils {
    public static Vector3f lineIntersection(Vector3f p0, Vector3f p1, Vector3f p2, Vector3f p3) {
        float epsilon = 0.001f;

        float a0 = p1.y() - p0.y();
        float b0 = p0.x() - p1.x();

        float a1 = p3.y() - p2.y();
        float b1 = p2.x() - p3.x();

        var det = a0 * b1 - a1 * b0;
        if (det > -epsilon && det < epsilon) {
            return null;
        } else {
            var c0 = a0 * p0.x + b0 * p0.y;
            var c1 = a1 * p2.x + b1 * p2.y;

            var x = (b1 * c0 - b0 * c1) / det;
            var y = (a0 * c1 - a1 * c0) / det;
            return new Vector3f(x, y, 0);
        }
    }
}
