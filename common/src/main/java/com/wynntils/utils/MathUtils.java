/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

public class MathUtils {
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    public static float inverselerp(float a, float b, float value) {
        return (value - a) / (b - a);
    }

    public static double inverselerp(double a, double b, double value) {
        return (value - a) / (b - a);
    }
}
