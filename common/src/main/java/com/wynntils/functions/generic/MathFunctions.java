/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.templates.annotations.TemplateFunction;
import java.util.Arrays;

@SuppressWarnings("unused") // Functions are accessed via reflection
public final class MathFunctions {
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

    // TODO: MORE
    //
    //
    //    public static class SafeDivideFunction extends GenericFunction<Double> {
    //        @Override
    //        public Double getValue(FunctionArguments arguments) {
    //            double dividend = arguments.getArgument("dividend").getDoubleValue();
    //            double divisor = arguments.getArgument("divisor").getDoubleValue();
    //            double fallback = arguments.getArgument("fallback").getDoubleValue();
    //            if (divisor == 0d) return fallback;
    //
    //            return dividend / divisor;
    //        }
    //
    //        @Override
    //        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
    //            return new FunctionArguments.RequiredArgumentBuilder(List.of(
    //                    new Argument<>("dividend", Number.class, null),
    //                    new Argument<>("divisor", Number.class, null),
    //                    new Argument<>("fallback", Number.class, null)));
    //        }
    //
    //        @Override
    //        protected List<String> getAliases() {
    //            return List.of("safe_div");
    //        }
    //    }
    //
    //    public static class NaturalLogFunction extends GenericFunction<Double> {
    //        @Override
    //        public Double getValue(FunctionArguments arguments) {
    //            return Math.log(arguments.getArgument("value").getDoubleValue());
    //        }
    //
    //        @Override
    //        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
    //            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class,
    // null)));
    //        }
    //
    //        @Override
    //        protected List<String> getAliases() {
    //            return List.of("ln");
    //        }
    //    }
    //
    //    public static class LogFunction extends GenericFunction<Double> {
    //        @Override
    //        public Double getValue(FunctionArguments arguments) {
    //            double value = arguments.getArgument("value").getDoubleValue();
    //            double base = arguments.getArgument("base").getDoubleValue();
    //
    //            return Math.log(value) / Math.log(base);
    //        }
    //
    //        @Override
    //        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
    //            return new FunctionArguments.RequiredArgumentBuilder(
    //                    List.of(new Argument<>("value", Number.class, null), new Argument<>("base", Number.class,
    // null)));
    //        }
    //    }
    //
    //    public static class MapFunction extends GenericFunction<Double> {
    //        @Override
    //        public Double getValue(FunctionArguments arguments) {
    //            double value = arguments.getArgument("value").getDoubleValue();
    //            double inMin = arguments.getArgument("inMin").getDoubleValue();
    //            double inMax = arguments.getArgument("inMax").getDoubleValue();
    //            double outMin = arguments.getArgument("outMin").getDoubleValue();
    //            double outMax = arguments.getArgument("outMax").getDoubleValue();
    //            double inWidth = inMax - inMin;
    //            if (inWidth == 0d) return outMin;
    //
    //            return outMin + ((value - inMin) * (outMax - outMin) / inWidth);
    //        }
    //
    //        @Override
    //        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
    //            return new FunctionArguments.RequiredArgumentBuilder(List.of(
    //                    new Argument<>("value", Number.class, null),
    //                    new Argument<>("inMin", Number.class, null),
    //                    new Argument<>("inMax", Number.class, null),
    //                    new Argument<>("outMin", Number.class, null),
    //                    new Argument<>("outMax", Number.class, null)));
    //        }
    //    }
    //
    //    public static class WrapFunction extends GenericFunction<Double> {
    //        @Override
    //        public Double getValue(FunctionArguments arguments) {
    //            double value = arguments.getArgument("value").getDoubleValue();
    //            double min = arguments.getArgument("min").getDoubleValue();
    //            double max = arguments.getArgument("max").getDoubleValue();
    //            double width = max - min;
    //            if (width == 0d) return min;
    //
    //            double wrapped = (value - min) % width;
    //            if (wrapped < 0d) wrapped += width;
    //            return wrapped + min;
    //        }
    //
    //        @Override
    //        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
    //            return new FunctionArguments.RequiredArgumentBuilder(List.of(
    //                    new Argument<>("value", Number.class, null),
    //                    new Argument<>("min", Number.class, null),
    //                    new Argument<>("max", Number.class, null)));
    //        }
    //    }
    //
    //    public static class PiFunction extends GenericFunction<Double> {
    //        @Override
    //        public Double getValue(FunctionArguments arguments) {
    //            return Math.PI;
    //        }
    //
    //        @Override
    //        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
    //            return new FunctionArguments.RequiredArgumentBuilder(List.of());
    //        }
    //    }
    //
    //    public static class EulerFunction extends GenericFunction<Double> {
    //        @Override
    //        public Double getValue(FunctionArguments arguments) {
    //            return Math.E;
    //        }
    //
    //        @Override
    //        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
    //            return new FunctionArguments.RequiredArgumentBuilder(List.of());
    //        }
    //
    //        @Override
    //        protected List<String> getAliases() {
    //            return List.of("e");
    //        }
    //    }
    //
    //    public static class DecToHexFunction extends GenericFunction<String> {
    //        private static final String LONG_MIN_HEX = "-8000000000000000";
    //
    //        @Override
    //        public String getValue(FunctionArguments arguments) {
    //            long value = arguments.getArgument("value").getLongValue();
    //            if (value < 0) {
    //                if (value == Long.MIN_VALUE) return LONG_MIN_HEX;
    //                return "-" + Long.toHexString(-value).toUpperCase(Locale.ROOT);
    //            }
    //
    //            return Long.toHexString(value).toUpperCase(Locale.ROOT);
    //        }
    //
    //        @Override
    //        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
    //            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class,
    // null)));
    //        }
    //    }
    //
    //    public static class HexToDecFunction extends GenericFunction<Long> {
    //        @Override
    //        public Long getValue(FunctionArguments arguments) {
    //            String rawValue = arguments.getArgument("hex").getStringValue().trim();
    //            boolean isNegative = rawValue.startsWith("-");
    //            String normalized = isNegative ? rawValue.substring(1) : rawValue;
    //
    //            if (normalized.startsWith("0x") || normalized.startsWith("0X")) {
    //                normalized = normalized.substring(2);
    //            } else if (normalized.startsWith("#")) {
    //                normalized = normalized.substring(1);
    //            }
    //
    //            if (normalized.isEmpty()) return 0L;
    //
    //            try {
    //                BigInteger parsed = new BigInteger(normalized, 16);
    //                if (isNegative) {
    //                    parsed = parsed.negate();
    //                }
    //
    //                if (parsed.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0
    //                        || parsed.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0) {
    //                    return 0L;
    //                }
    //
    //                return parsed.longValue();
    //            } catch (NumberFormatException ignored) {
    //                return 0L;
    //            }
    //        }
    //
    //        @Override
    //        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
    //            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("hex", String.class,
    // null)));
    //        }
    //    }
    //
    //    public static class IsFiniteFunction extends GenericFunction<Boolean> {
    //        @Override
    //        public Boolean getValue(FunctionArguments arguments) {
    //            return Double.isFinite(arguments.getArgument("value").getDoubleValue());
    //        }
    //
    //        @Override
    //        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
    //            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class,
    // null)));
    //        }
    //    }
    //
    //    public static class IsNanFunction extends GenericFunction<Boolean> {
    //        @Override
    //        public Boolean getValue(FunctionArguments arguments) {
    //            return Double.isNaN(arguments.getArgument("value").getDoubleValue());
    //        }
    //
    //        @Override
    //        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
    //            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class,
    // null)));
    //        }
    //    }
    //
    //    public static class IsInfiniteFunction extends GenericFunction<Boolean> {
    //        @Override
    //        public Boolean getValue(FunctionArguments arguments) {
    //            return Double.isInfinite(arguments.getArgument("value").getDoubleValue());
    //        }
    //
    //        @Override
    //        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
    //            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class,
    // null)));
    //        }
    //    }
}
