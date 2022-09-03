/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import java.util.Locale;

public class MathUtils {
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

    public static int integerFromRoman(String numeral) {
        int num = 0;
        switch (numeral.toUpperCase(Locale.ROOT)) {
            case "I":
                num = 1;
                break;
            case "II":
                num = 2;
                break;
            case "III":
                num = 3;
                break;
            case "IV":
                num = 4;
                break;
            case "V":
                num = 5;
                break;
            case "VI":
                num = 6;
                break;
            case "VII":
                num = 7;
                break;
            case "VIII":
                num = 8;
                break;
            case "IX":
                num = 9;
                break;
            case "X":
                num = 10;
                break;
        }
        return num;
    }

    /**
     * Calculates the integer log base 2 of a double.
     * <p>Provides a faster alternative to {@code (int)(Math.log(value)/Math.log(2))} without floating point introduced errors.</p>
     * @param value to calculate the log of.
     * @return Rounded down log base 2 of value.
     */
    public static int log2(double value) {
        final int exponent = Math.getExponent(value);
        return exponent >= Double.MIN_EXPONENT
                ? exponent
                : (Double.MIN_EXPONENT
                        - Long.numberOfLeadingZeros(Double.doubleToRawLongBits(value) & 0x000fffffffffffffL)
                        + 11);
    }
}
