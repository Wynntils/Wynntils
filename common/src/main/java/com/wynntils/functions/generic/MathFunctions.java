/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.core.functions.GenericFunction;
import com.wynntils.core.functions.arguments.FunctionArguments;
import java.util.List;

public final class MathFunctions {
    public static class AddFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return arguments.getArgument("first").getDoubleValue()
                    + arguments.getArgument("second").getDoubleValue();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.Builder(List.of(
                    new FunctionArguments.Argument<>("first", Number.class, null),
                    new FunctionArguments.Argument<>("second", Number.class, null)));
        }
    }

    public static class SubtractFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return arguments.getArgument("first").getDoubleValue()
                    - arguments.getArgument("second").getDoubleValue();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.Builder(List.of(
                    new FunctionArguments.Argument<>("first", Number.class, null),
                    new FunctionArguments.Argument<>("second", Number.class, null)));
        }
    }

    public static class MultiplyFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return arguments.getArgument("first").getDoubleValue()
                    * arguments.getArgument("second").getDoubleValue();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.Builder(List.of(
                    new FunctionArguments.Argument<>("first", Number.class, null),
                    new FunctionArguments.Argument<>("second", Number.class, null)));
        }
    }

    public static class DivideFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return arguments.getArgument("first").getDoubleValue()
                    / arguments.getArgument("second").getDoubleValue();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.Builder(List.of(
                    new FunctionArguments.Argument<>("first", Number.class, null),
                    new FunctionArguments.Argument<>("second", Number.class, null)));
        }
    }

    public static class ModuloFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return arguments.getArgument("first").getDoubleValue()
                    % arguments.getArgument("second").getDoubleValue();
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.Builder(List.of(
                    new FunctionArguments.Argument<>("first", Number.class, null),
                    new FunctionArguments.Argument<>("second", Number.class, null)));
        }
    }

    public static class PowerFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Math.pow(
                    arguments.getArgument("first").getDoubleValue(),
                    arguments.getArgument("second").getDoubleValue());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.Builder(List.of(
                    new FunctionArguments.Argument<>("first", Number.class, null),
                    new FunctionArguments.Argument<>("second", Number.class, null)));
        }
    }

    public static class SquareRootFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Math.sqrt(arguments.getArgument("value").getDoubleValue());
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.Builder(List.of(new FunctionArguments.Argument("value", Number.class, null)));
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
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.Builder(List.of(
                    new FunctionArguments.Argument<>("value", Number.class, null),
                    new FunctionArguments.Argument<>("decimals", Integer.class, null)));
        }
    }
}
