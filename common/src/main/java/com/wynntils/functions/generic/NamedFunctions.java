/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.core.consumers.functions.GenericFunction;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.utils.type.NamedValue;
import java.util.List;

public final class NamedFunctions {
    public static class NameFunction extends GenericFunction<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return arguments.getArgument("named").getNamedValue().name();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("named", NamedValue.class, null)));
        }
    }

    public static class ValueFunction extends GenericFunction<Integer> {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return arguments.getArgument("named").getNamedValue().value();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("named", NamedValue.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("val");
        }
    }

    public static class NamedValueFunction extends GenericFunction<NamedValue> {
        @Override
        public NamedValue getValue(FunctionArguments arguments) {
            return new NamedValue(
                    arguments.getArgument("name").getStringValue(),
                    arguments.getArgument("value").getIntegerValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("name", String.class, null),
                    new FunctionArguments.Argument<>("value", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("named");
        }
    }
}
