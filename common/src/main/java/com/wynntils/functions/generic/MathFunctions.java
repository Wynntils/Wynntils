/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.core.consumers.functions.GenericFunction;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.core.consumers.functions.arguments.ListArgument;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class MathFunctions {
    public static class AddFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            List<Number> values = arguments.getArgument("values").getNumberList();

            return values.stream().mapToDouble(Number::doubleValue).sum();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new ListArgument<>("values", Number.class)));
        }
    }

    public static class SubtractFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return arguments.getArgument("first").getDoubleValue()
                    - arguments.getArgument("second").getDoubleValue();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("first", Number.class, null), new Argument<>("second", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("sub");
        }
    }

    public static class MultiplyFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            List<Number> values = arguments.getArgument("values").getNumberList();

            return values.stream().mapToDouble(Number::doubleValue).reduce(1, (a, b) -> a * b);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new ListArgument<>("values", Number.class)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("mul");
        }
    }

    public static class DivideFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return arguments.getArgument("dividend").getDoubleValue()
                    / arguments.getArgument("divisor").getDoubleValue();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("dividend", Number.class, null), new Argument<>("divisor", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("div");
        }
    }

    public static class ModuloFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return arguments.getArgument("dividend").getDoubleValue()
                    % arguments.getArgument("divisor").getDoubleValue();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("dividend", Number.class, null), new Argument<>("divisor", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("mod");
        }
    }

    public static class PowerFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Math.pow(
                    arguments.getArgument("base").getDoubleValue(),
                    arguments.getArgument("exponent").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("base", Number.class, null), new Argument<>("exponent", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("pow");
        }
    }

    public static class SquareRootFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Math.sqrt(arguments.getArgument("value").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("sqrt");
        }
    }

    public static class MaxFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            List<Number> values = arguments.getArgument("values").getNumberList();
            return values.stream().mapToDouble(Number::doubleValue).max().orElse(0);
            // .orElse(0) is safer because max() returns OptionalDouble, but will probably never be used
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new ListArgument<>("values", Number.class)));
        }
    }

    public static class MinFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            List<Number> values = arguments.getArgument("values").getNumberList();
            return values.stream().mapToDouble(Number::doubleValue).min().orElse(0);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new ListArgument<>("values", Number.class)));
        }
    }

    public static class RoundFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            double roundingValue =
                    Math.pow(10, arguments.getArgument("decimals").getIntegerValue());
            return Math.round(arguments.getArgument("value").getDoubleValue() * roundingValue) / roundingValue;
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", Number.class, null), new Argument<>("decimals", Integer.class, null)));
        }
    }

    public static class IntegerFunction extends GenericFunction<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return arguments.getArgument("value").getIntegerValue();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("int");
        }
    }

    public static class LongFunction extends GenericFunction<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            return arguments.getArgument("value").getLongValue();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class, null)));
        }
    }

    public static class RandomFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            double min = arguments.getArgument("min").getIntegerValue();
            double max = arguments.getArgument("max").getIntegerValue();
            return (Math.random() * (max - min)) + min;
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("min", Number.class, null), new Argument<>("max", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("rand");
        }
    }

    public static class AbsFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Math.abs(arguments.getArgument("value").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class, null)));
        }
    }

    public static class FloorFunction extends GenericFunction<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            return (long) Math.floor(arguments.getArgument("value").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class, null)));
        }
    }

    public static class CeilFunction extends GenericFunction<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            return (long) Math.ceil(arguments.getArgument("value").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class, null)));
        }
    }

    public static class TruncFunction extends GenericFunction<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            return arguments.getArgument("value").getDoubleValue().longValue();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class, null)));
        }
    }

    public static class SignFunction extends GenericFunction<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return (int) Math.signum(arguments.getArgument("value").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class, null)));
        }
    }

    public static class ClampFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            double value = arguments.getArgument("value").getDoubleValue();
            double min = arguments.getArgument("min").getDoubleValue();
            double max = arguments.getArgument("max").getDoubleValue();
            if (min > max) {
                double temp = min;
                min = max;
                max = temp;
            }

            return Math.max(min, Math.min(max, value));
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", Number.class, null),
                    new Argument<>("min", Number.class, null),
                    new Argument<>("max", Number.class, null)));
        }
    }

    public static class SafeDivFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            double dividend = arguments.getArgument("dividend").getDoubleValue();
            double divisor = arguments.getArgument("divisor").getDoubleValue();
            double fallback = arguments.getArgument("fallback").getDoubleValue();
            if (divisor == 0d) return fallback;

            return dividend / divisor;
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("dividend", Number.class, null),
                    new Argument<>("divisor", Number.class, null),
                    new Argument<>("fallback", Number.class, null)));
        }
    }

    public static class SinFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Math.sin(arguments.getArgument("radians").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("radians", Number.class, null)));
        }
    }

    public static class CosFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Math.cos(arguments.getArgument("radians").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("radians", Number.class, null)));
        }
    }

    public static class TanFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Math.tan(arguments.getArgument("radians").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("radians", Number.class, null)));
        }
    }

    public static class AsinFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Math.asin(arguments.getArgument("value").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class, null)));
        }
    }

    public static class AcosFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Math.acos(arguments.getArgument("value").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class, null)));
        }
    }

    public static class AtanFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Math.atan(arguments.getArgument("value").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class, null)));
        }
    }

    public static class AtanTwoFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Math.atan2(
                    arguments.getArgument("y").getDoubleValue(),
                    arguments.getArgument("x").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("y", Number.class, null), new Argument<>("x", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("atanxy");
        }
    }

    public static class RadFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Math.toRadians(arguments.getArgument("degrees").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("degrees", Number.class, null)));
        }
    }

    public static class DegFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Math.toDegrees(arguments.getArgument("radians").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("radians", Number.class, null)));
        }
    }

    public static class LnFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Math.log(arguments.getArgument("value").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class, null)));
        }
    }

    public static class LogFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            double value = arguments.getArgument("value").getDoubleValue();
            double base = arguments.getArgument("base").getDoubleValue();

            return Math.log(value) / Math.log(base);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("value", Number.class, null), new Argument<>("base", Number.class, null)));
        }
    }

    public static class ExpFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Math.exp(arguments.getArgument("value").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class, null)));
        }
    }

    public static class LerpFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            double start = arguments.getArgument("start").getDoubleValue();
            double end = arguments.getArgument("end").getDoubleValue();
            double t = arguments.getArgument("t").getDoubleValue();

            return start + (end - start) * t;
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("start", Number.class, null),
                    new Argument<>("end", Number.class, null),
                    new Argument<>("t", Number.class, null)));
        }
    }

    public static class InvLerpFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            double value = arguments.getArgument("value").getDoubleValue();
            double start = arguments.getArgument("start").getDoubleValue();
            double end = arguments.getArgument("end").getDoubleValue();
            double width = end - start;
            if (width == 0d) return 0d;

            return (value - start) / width;
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", Number.class, null),
                    new Argument<>("start", Number.class, null),
                    new Argument<>("end", Number.class, null)));
        }
    }

    public static class SmoothstepFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            double value = arguments.getArgument("value").getDoubleValue();
            double start = arguments.getArgument("start").getDoubleValue();
            double end = arguments.getArgument("end").getDoubleValue();
            double width = end - start;
            if (width == 0d) return 0d;

            double t = (value - start) / width;
            t = Math.max(0d, Math.min(1d, t));
            return t * t * (3d - 2d * t);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", Number.class, null),
                    new Argument<>("start", Number.class, null),
                    new Argument<>("end", Number.class, null)));
        }
    }

    public static class MapFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            double value = arguments.getArgument("value").getDoubleValue();
            double inMin = arguments.getArgument("inMin").getDoubleValue();
            double inMax = arguments.getArgument("inMax").getDoubleValue();
            double outMin = arguments.getArgument("outMin").getDoubleValue();
            double outMax = arguments.getArgument("outMax").getDoubleValue();
            double inWidth = inMax - inMin;
            if (inWidth == 0d) return outMin;

            return outMin + ((value - inMin) * (outMax - outMin) / inWidth);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", Number.class, null),
                    new Argument<>("inMin", Number.class, null),
                    new Argument<>("inMax", Number.class, null),
                    new Argument<>("outMin", Number.class, null),
                    new Argument<>("outMax", Number.class, null)));
        }
    }

    public static class MapClampedFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            double value = arguments.getArgument("value").getDoubleValue();
            double inMin = arguments.getArgument("inMin").getDoubleValue();
            double inMax = arguments.getArgument("inMax").getDoubleValue();
            double outMin = arguments.getArgument("outMin").getDoubleValue();
            double outMax = arguments.getArgument("outMax").getDoubleValue();
            double inWidth = inMax - inMin;
            if (inWidth == 0d) return outMin;

            double remapped = outMin + ((value - inMin) * (outMax - outMin) / inWidth);
            double lower = Math.min(outMin, outMax);
            double upper = Math.max(outMin, outMax);
            return Math.max(lower, Math.min(upper, remapped));
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", Number.class, null),
                    new Argument<>("inMin", Number.class, null),
                    new Argument<>("inMax", Number.class, null),
                    new Argument<>("outMin", Number.class, null),
                    new Argument<>("outMax", Number.class, null)));
        }
    }

    public static class WrapFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            double value = arguments.getArgument("value").getDoubleValue();
            double min = arguments.getArgument("min").getDoubleValue();
            double max = arguments.getArgument("max").getDoubleValue();
            double width = max - min;
            if (width == 0d) return min;

            double wrapped = (value - min) % width;
            if (wrapped < 0d) wrapped += width;
            return wrapped + min;
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", Number.class, null),
                    new Argument<>("min", Number.class, null),
                    new Argument<>("max", Number.class, null)));
        }
    }

    public static class AvgFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            List<Number> values = arguments.getArgument("values").getNumberList();
            if (values.isEmpty()) return 0d;

            return values.stream().mapToDouble(Number::doubleValue).average().orElse(0d);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new ListArgument<>("values", Number.class)));
        }
    }

    public static class MedianFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            List<Number> rawValues = arguments.getArgument("values").getNumberList();
            if (rawValues.isEmpty()) return 0d;

            List<Double> values =
                    new ArrayList<>(rawValues.stream().map(Number::doubleValue).toList());
            values.sort(Comparator.naturalOrder());

            int middleIndex = values.size() / 2;
            if (values.size() % 2 == 1) {
                return values.get(middleIndex);
            }

            return (values.get(middleIndex - 1) + values.get(middleIndex)) / 2d;
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new ListArgument<>("values", Number.class)));
        }
    }

    public static class VarianceFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            List<Number> values = arguments.getArgument("values").getNumberList();
            if (values.isEmpty()) return 0d;

            double mean =
                    values.stream().mapToDouble(Number::doubleValue).average().orElse(0d);
            return values.stream()
                            .mapToDouble(value -> {
                                double delta = value.doubleValue() - mean;
                                return delta * delta;
                            })
                            .sum()
                    / values.size();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new ListArgument<>("values", Number.class)));
        }
    }

    public static class StddevFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            List<Number> values = arguments.getArgument("values").getNumberList();
            if (values.isEmpty()) return 0d;

            double mean =
                    values.stream().mapToDouble(Number::doubleValue).average().orElse(0d);
            double variance = values.stream()
                            .mapToDouble(value -> {
                                double delta = value.doubleValue() - mean;
                                return delta * delta;
                            })
                            .sum()
                    / values.size();
            return Math.sqrt(variance);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new ListArgument<>("values", Number.class)));
        }
    }

    public static class RangeFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            List<Number> values = arguments.getArgument("values").getNumberList();
            if (values.isEmpty()) return 0d;

            double min = values.stream().mapToDouble(Number::doubleValue).min().orElse(0d);
            double max = values.stream().mapToDouble(Number::doubleValue).max().orElse(0d);
            return max - min;
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new ListArgument<>("values", Number.class)));
        }
    }

    public static class PercentileFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            List<Number> rawValues = arguments.getArgument("values").getNumberList();
            if (rawValues.isEmpty()) return 0d;

            List<Double> values =
                    new ArrayList<>(rawValues.stream().map(Number::doubleValue).toList());
            values.sort(Comparator.naturalOrder());

            double percent = arguments.getArgument("percent").getDoubleValue();
            if (percent <= 0d) return values.getFirst();
            if (percent >= 100d) return values.getLast();

            double position = (percent / 100d) * (values.size() - 1);
            int lowerIndex = (int) Math.floor(position);
            int upperIndex = (int) Math.ceil(position);
            if (lowerIndex == upperIndex) {
                return values.get(lowerIndex);
            }

            double lowerValue = values.get(lowerIndex);
            double upperValue = values.get(upperIndex);
            double factor = position - lowerIndex;
            return lowerValue + (upperValue - lowerValue) * factor;
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("percent", Number.class, null), new ListArgument<>("values", Number.class)));
        }
    }

    public static class PiFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Math.PI;
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of());
        }
    }

    public static class EFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Math.E;
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of());
        }
    }

    public static class DecToHexFunction extends GenericFunction<String> {
        private static final String LONG_MIN_HEX = "-8000000000000000";

        @Override
        public String getValue(FunctionArguments arguments) {
            long value = arguments.getArgument("value").getLongValue();
            if (value < 0) {
                if (value == Long.MIN_VALUE) return LONG_MIN_HEX;
                return "-" + Long.toHexString(-value).toUpperCase(Locale.ROOT);
            }

            return Long.toHexString(value).toUpperCase(Locale.ROOT);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class, null)));
        }
    }

    public static class HexToDecFunction extends GenericFunction<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            String rawValue = arguments.getArgument("hex").getStringValue().trim();
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

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("hex", String.class, null)));
        }
    }

    public static class IsFiniteFunction extends GenericFunction<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return Double.isFinite(arguments.getArgument("value").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class, null)));
        }
    }

    public static class IsNanFunction extends GenericFunction<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return Double.isNaN(arguments.getArgument("value").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class, null)));
        }
    }

    public static class IsInfiniteFunction extends GenericFunction<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return Double.isInfinite(arguments.getArgument("value").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class, null)));
        }
    }
}
