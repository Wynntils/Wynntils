/*
 * Copyright © Wynntils 2021-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.joml.Vector2f;

public final class MathUtils {
    private static final Map<Character, Integer> ROMAN_NUMERALS_MAP =
            Map.of('I', 1, 'V', 5, 'X', 10, 'L', 50, 'C', 100, 'D', 500, 'M', 1000);

    private static final TreeMap<Integer, String> INT_TO_ROMAN_MAP = new TreeMap<>(Map.ofEntries(
            Map.entry(1000, "M"),
            Map.entry(900, "CM"),
            Map.entry(500, "D"),
            Map.entry(400, "CD"),
            Map.entry(100, "C"),
            Map.entry(90, "XC"),
            Map.entry(50, "L"),
            Map.entry(40, "XL"),
            Map.entry(10, "X"),
            Map.entry(9, "IX"),
            Map.entry(5, "V"),
            Map.entry(4, "IV"),
            Map.entry(1, "I")));

    public static int floor(float value) {
        int i = (int) value;
        return value < (float) i ? i - 1 : i;
    }

    public static int floor(double value) {
        int i = (int) value;
        return value < (double) i ? i - 1 : i;
    }

    public static float frac(float number) {
        return number - (float) floor(number);
    }

    public static float lerp(float start, float end, float delta) {
        return start + (end - start) * delta;
    }

    public static double lerp(double start, double end, double delta) {
        return start + (end - start) * delta;
    }

    public static float inverseLerp(float start, float end, float value) {
        return (value - start) / (end - start);
    }

    public static double inverseLerp(double start, double end, double value) {
        return (value - start) / (end - start);
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

        return normalized
                .chars()
                .map(c -> ROMAN_NUMERALS_MAP.getOrDefault((char) c, 0))
                .sum();
    }

    public static String toRoman(int number) {
        int l = INT_TO_ROMAN_MAP.floorKey(number);
        if (number == l) {
            return INT_TO_ROMAN_MAP.get(number);
        }
        return INT_TO_ROMAN_MAP.get(l) + toRoman(number - l);
    }

    public static float map(float sourceNumber, float fromA, float fromB, float toA, float toB) {
        return MathUtils.lerp(toA, toB, MathUtils.inverseLerp(fromA, fromB, sourceNumber));
    }

    public static float magnitude(float x, float y) {
        return (float) Math.sqrt(x * x + y * y);
    }

    public static double magnitude(double x, double y) {
        return Math.sqrt(x * x + y * y);
    }

    public static boolean isInside(int testX, int testZ, int x1, int x2, int z1, int z2) {
        return x1 <= testX && testX <= x2 && z1 <= testZ && testZ <= z2;
    }

    public static boolean boundingBoxIntersects(
            int aX1, int aX2, int aZ1, int aZ2, int bX1, int bX2, int bZ1, int bZ2) {
        boolean xIntersects = aX1 < bX2 && bX1 < aX2;
        boolean zIntersects = aZ1 < bZ2 && bZ1 < aZ2;
        return xIntersects && zIntersects;
    }

    public static float signedArea(Vector2f p0, Vector2f p1, Vector2f p2) {
        return (p1.x() - p0.x()) * (p2.y() - p0.y()) - (p2.x() - p0.x()) * (p1.y() - p0.y());
    }
}
