/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.core.functions.GenericFunction;
import com.wynntils.core.functions.arguments.FunctionArguments;
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
        public List<String> getAliases() {
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
        public List<String> getAliases() {
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
        public List<String> getAliases() {
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
        public List<String> getAliases() {
            return List.of("lte");
        }
    }

    public static class MoreThanFunction extends GenericFunction<Boolean> {
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
        public List<String> getAliases() {
            return List.of("mt");
        }
    }

    public static class MoreThanOrEqualsFunction extends GenericFunction<Boolean> {
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
        public List<String> getAliases() {
            return List.of("mte");
        }
    }
}
