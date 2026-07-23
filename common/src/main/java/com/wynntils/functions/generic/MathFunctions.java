/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.templates.annotations.TemplateFunction;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Locale;

@SuppressWarnings("unused") // Functions are accessed via reflection
public class MathFunctions {
    @TemplateFunction(
            name = "add",
            aliases = {"sum", "plus"},
            isPure = true)
    public static double addFunction(double a, double b) {
        return a + b;
    }

    @TemplateFunction(
            name = "add",
            aliases = {"sum", "plus"},
            isPure = true)
    public static double addFunction(double... values) {
        double sum = 0;
        for (double value : values) {
            sum += value;
        }

        return sum;
    }

    @TemplateFunction(name = "subtract", aliases = "sub", isPure = true)
    public static double subtractFunction(double a, double b) {
        return a - b;
    }

    @TemplateFunction(name = "multiply", aliases = "mul", isPure = true)
    public static double multiplyFunction(double a, double b) {
        return a * b;
    }

    @TemplateFunction(name = "multiply", aliases = "mul", isPure = true)
    public static double multiplyFunction(double... values) {
        double sum = 1;
        for (double value : values) {
            sum *= value;
        }

        return sum;
    }

    @TemplateFunction(name = "divide", aliases = "div", isPure = true)
    public static double divideFunction(double dividend, double divisor) {
        return dividend / divisor;
    }

    @TemplateFunction(name = "modulo", aliases = "mod", isPure = true)
    public static double moduloFunction(double dividend, double divisor) {
        return dividend % divisor;
    }

    @TemplateFunction(name = "power", aliases = "pow", isPure = true)
    public static double powerFunction(double base, double exponent) {
        return Math.pow(base, exponent);
    }

    @TemplateFunction(name = "square_root", aliases = "sqrt", isPure = true)
    public static double squareRootFunction(double value) {
        return Math.sqrt(value);
    }

    @TemplateFunction(name = "max", isPure = true)
    public static double maxFunction(double a, double b) {
        return Math.max(a, b);
    }

    @TemplateFunction(name = "max", isPure = true)
    public static double maxFunction(double... values) {
        return Arrays.stream(values).max().orElse(0);
    }

    @TemplateFunction(name = "min", isPure = true)
    public static double minFunction(double a, double b) {
        return Math.min(a, b);
    }

    @TemplateFunction(name = "min", isPure = true)
    public static double minFunction(double... values) {
        return Arrays.stream(values).min().orElse(0);
    }

    @TemplateFunction(name = "round", isPure = true)
    public static double roundFunction(int decimals, double value) {
        double roundingValue = Math.pow(10, decimals);
        return Math.round(value * roundingValue) / roundingValue;
    }

    @TemplateFunction(name = "integer", aliases = "int", isPure = true)
    public static int integerFunction(Number number) {
        return number.intValue();
    }

    @TemplateFunction(name = "long", isPure = true)
    public static long longFunction(Number number) {
        return number.longValue();
    }

    @TemplateFunction(name = "random", aliases = "rand")
    public static double randomFunction(int min, int max) {
        return (Math.random() * (max - min)) + min;
    }

    @TemplateFunction(name = "abs", isPure = true)
    public static double absFunction(double value) {
        return Math.abs(value);
    }

    @TemplateFunction(name = "floor", isPure = true)
    public static long floorFunction(double value) {
        return (long) Math.floor(value);
    }

    @TemplateFunction(name = "ceil", isPure = true)
    public static long ceilFunction(double value) {
        return (long) Math.ceil(value);
    }

    @TemplateFunction(name = "clamp", isPure = true)
    public static double clampFunction(double value, double min, double max) {
        if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }

        return Math.max(min, Math.min(max, value));
    }

    @TemplateFunction(name = "safe_divide", aliases = "safe_div", isPure = true)
    public static double safeDivideFunction(double dividend, double divisor, double fallback) {
        if (divisor == 0d) return fallback;

        return dividend / divisor;
    }


    @TemplateFunction(name = "natural_log", aliases = "ln", isPure = true)
    public static double naturalLogFunction(double value) {
        return Math.log(value);
    }

    @TemplateFunction(name = "log", isPure = true)
    public static double logFunction(double value, double base) {
        return Math.log(value) / Math.log(base);
    }


    @TemplateFunction(name = "map", isPure = true)
    public static double mapFunction(double value, double inMin, double inMax, double outMin, double outMax) {
        double inWidth = inMax - inMin;
        if (inWidth == 0d) return outMin;

        return outMin + ((value - inMin) * (outMax - outMin) / inWidth);
    }

    @TemplateFunction(name = "wrap", isPure = true)
    public static double wrapFunction(double value, double min, double max) {
        double width = max - min;
        if (width == 0d) return min;

        double wrapped = (value - min) % width;
        if (wrapped < 0d) wrapped += width;
        return wrapped + min;
    }

    @TemplateFunction(name = "pi", isPure = true)
    public static double piFunction() {
        return Math.PI;
    }


    @TemplateFunction(name = "euler", aliases = "e", isPure = true)
    public static double eulerFunction() {
        return Math.E;

    }

    private static final String LONG_MIN_HEX = "-8000000000000000";


    @TemplateFunction(name = "dec_to_hex", isPure = true)
    public static String decToHexFunction(long value) {
        if (value < 0) {
            if (value == Long.MIN_VALUE) return LONG_MIN_HEX;
            return "-" + Long.toHexString(-value).toUpperCase(Locale.ROOT);
        }

        return Long.toHexString(value).toUpperCase(Locale.ROOT);
    }


    @TemplateFunction(name = "hex_to_dec", isPure = true)
    public static long hexToDecFunction(String rawValue) {
        boolean isNegative = rawValue.startsWith("-");
        String normalized = isNegative ? rawValue.substring(1) : rawValue;

        if (normalized.startsWith("0x") || normalized.startsWith("0X")) {
            normalized = normalized.substring(2);
        } else if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }

        if (normalized.isEmpty()) return 0L;

        try {
            BigInteger parsed = new BigInteger(normalized, 16);
            if (isNegative) {
                parsed = parsed.negate();
            }

            if (parsed.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0
                    || parsed.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0) {
                return 0L;
            }

            return parsed.longValue();
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    @TemplateFunction(name = "is_finite", isPure = true)
    public static boolean isFiniteFunction(double value) {
        return Double.isFinite(value);
    }

    @TemplateFunction(name = "is_nan", isPure = true)
    public static boolean isNanFunction(double value) {
        return Double.isNaN(value);

    }

    @TemplateFunction(name = "is_infinite", isPure = true)
    public static boolean isInfiniteFunction(double value){
        return Double.isInfinite(value);
    }
}
