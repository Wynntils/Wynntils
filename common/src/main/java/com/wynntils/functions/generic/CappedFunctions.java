/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.core.consumers.functions.GenericFunction;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.utils.type.CappedValue;
import java.util.List;

public final class CappedFunctions {
    public static class CurrentFunction extends GenericFunction<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return arguments.getArgument("capped").getCappedValue().current();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("capped", CappedValue.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("curr");
        }
    }

    public static class CapFunction extends GenericFunction<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return arguments.getArgument("capped").getCappedValue().max();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("capped", CappedValue.class, null)));
        }
    }

    public static class RemainingFunction extends GenericFunction<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return arguments.getArgument("capped").getCappedValue().getRemaining();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("capped", CappedValue.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("rem");
        }
    }

    public static class PercentageFunction extends GenericFunction<Double> {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return arguments.getArgument("capped").getCappedValue().getPercentage();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("capped", CappedValue.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("pct");
        }
    }

    public static class AtCapFunction extends GenericFunction<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return arguments.getArgument("capped").getCappedValue().isAtCap();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("capped", CappedValue.class, null)));
        }
    }

    public static class CappedFunction extends GenericFunction<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return new CappedValue(
                    arguments.getArgument("current").getIntegerValue(),
                    arguments.getArgument("cap").getIntegerValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("current", Number.class, null), new Argument<>("cap", Number.class, null)));
        }
    }
}
