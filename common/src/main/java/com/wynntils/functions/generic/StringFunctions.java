/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.core.functions.GenericFunction;
import com.wynntils.core.functions.arguments.FunctionArguments;
import com.wynntils.utils.StringUtils;
import java.util.List;

public class StringFunctions {
    public static class FormatFunction extends GenericFunction<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return StringUtils.integerToShortString(
                    arguments.getArgument("value").getIntegerValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument("value", Number.class, null)));
        }
    }

    public static class StringFunction extends GenericFunction<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return arguments.getArgument("value").getValue().toString();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument("value", Number.class, null)));
        }

        @Override
        public List<String> getAliases() {
            return List.of("str");
        }
    }

    public static class ConcatFunction extends GenericFunction<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return arguments.getArgument("first").getStringValue()
                    + arguments.getArgument("second").getStringValue();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new FunctionArguments.Argument("first", String.class, null),
                    new FunctionArguments.Argument("second", String.class, null)));
        }
    }
}
