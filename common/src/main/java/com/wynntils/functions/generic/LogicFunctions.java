/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.core.consumers.functions.GenericFunction;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import java.util.List;
import java.util.Objects;

public class LogicFunctions {
    public static class EqualsFunction extends GenericFunction<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return Objects.equals(
                    arguments.getArgument("first").getDoubleValue(),
                    arguments.getArgument("second").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("first", Number.class, null),
                    new FunctionArguments.Argument<>("second", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("eq");
        }
    }

    public static class NotEqualsFunction extends GenericFunction<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return !Objects.equals(
                    arguments.getArgument("first").getDoubleValue(),
                    arguments.getArgument("second").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("first", Number.class, null),
                    new FunctionArguments.Argument<>("second", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("neq");
        }
    }

    public static class NotFunction extends GenericFunction<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return !arguments.getArgument("value").getBooleanValue();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("value", Boolean.class, null)));
        }
    }

    public static class AndFunction extends GenericFunction<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            List<Boolean> values =
                    arguments.<Boolean>getArgument("values").asList().getValues();

            return values.stream().allMatch(Boolean::booleanValue);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.ListArgument<>("values", Boolean.class)));
        }
    }

    public static class OrFunction extends GenericFunction<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            List<Boolean> values =
                    arguments.<Boolean>getArgument("values").asList().getValues();

            return values.stream().anyMatch(Boolean::booleanValue);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.ListArgument<>("values", Boolean.class)));
        }
    }

    public static class LessThanFunction extends GenericFunction<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return arguments.getArgument("first").getDoubleValue()
                    < arguments.getArgument("second").getDoubleValue();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("first", Number.class, null),
                    new FunctionArguments.Argument<>("second", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("lt");
        }
    }

    public static class LessThanOrEqualsFunction extends GenericFunction<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return arguments.getArgument("first").getDoubleValue()
                    <= arguments.getArgument("second").getDoubleValue();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("first", Number.class, null),
                    new FunctionArguments.Argument<>("second", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("lte", "less_than_equals", "leq");
        }
    }

    public static class GreaterThanFunction extends GenericFunction<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return arguments.getArgument("first").getDoubleValue()
                    > arguments.getArgument("second").getDoubleValue();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("first", Number.class, null),
                    new FunctionArguments.Argument<>("second", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("mt", "more_than", "gt");
        }
    }

    public static class GreaterThanOrEqualsFunction extends GenericFunction<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return arguments.getArgument("first").getDoubleValue()
                    >= arguments.getArgument("second").getDoubleValue();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("first", Number.class, null),
                    new FunctionArguments.Argument<>("second", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("mte", "more_than_equals", "greater_than_equals", "gte", "geq");
        }
    }
}
