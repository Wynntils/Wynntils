/*
 * Copyright © Wynntils 2024-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.core.consumers.functions.GenericFunction;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.utils.type.RangedValue;
import java.util.List;

public final class RangedFunctions {
    public static class RangedFunction extends GenericFunction<RangedValue> {
        @Override
        public RangedValue getValue(FunctionArguments arguments) {
            return new RangedValue(
                    arguments.getArgument("low").getIntegerValue(),
                    arguments.getArgument("high").getIntegerValue());
        }

        @Override
        protected FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("low", Integer.class, null), new Argument<>("high", Integer.class, null)));
        }
    }

    public static class RangeLowFunction extends GenericFunction<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return arguments.getArgument("range").getRangedValue().low();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("low");
        }

        @Override
        protected FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("range", RangedValue.class, null)));
        }
    }

    public static class RangeHighFunction extends GenericFunction<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return arguments.getArgument("range").getRangedValue().high();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("high");
        }

        @Override
        protected FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("range", RangedValue.class, null)));
        }
    }
}
