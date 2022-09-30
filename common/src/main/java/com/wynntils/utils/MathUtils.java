/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class MathUtils {

    private static final Map<Integer, Integer> romanNumeralsMap = new HashMap<>();

    static {
        romanNumeralsMap.put((int) 'I', 1);
        romanNumeralsMap.put((int) 'V', 5);
        romanNumeralsMap.put((int) 'X', 10);
        romanNumeralsMap.put((int) 'L', 50);
        romanNumeralsMap.put((int) 'C', 100);
        romanNumeralsMap.put((int) 'D', 500);
        romanNumeralsMap.put((int) 'M', 1000);
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    public static float inverseLerp(float a, float b, float value) {
        return (value - a) / (b - a);
    }

    public static double inverseLerp(double a, double b, double value) {
        return (value - a) / (b - a);
    }

    public static int clamp(int num, int min, int max) {
        if (num < min) {
            return min;
        } else {
            return Math.min(num, max);
        }
    }

    public static float clamp(float num, float min, float max) {
        if (num < min) {
            return min;
        } else {
            return Math.min(num, max);
        }
    }

    public static int integerFromRoman(String numeral) {
        String normalized = numeral.trim()
                .toUpperCase(Locale.ROOT)
                .replace("IV", "IIII")
                .replace("IX", "VIIII")
                .replace("XL", "XXXX")
                .replace("XC", "LXXXX")
                .replace("CD", "CCCC")
                .replace("CM", "DCCCC");

        return normalized.chars().map(c -> romanNumeralsMap.getOrDefault(c, 0)).sum();
    }

    public static float map(float sourceNumber, float fromA, float fromB, float toA, float toB) {
        return MathUtils.lerp(toA, toB, MathUtils.inverseLerp(fromA, fromB, sourceNumber));
    }

    public static float magnitude(float x, float y) {
        return (float) Math.sqrt(x * x + y * y);
    }
}
