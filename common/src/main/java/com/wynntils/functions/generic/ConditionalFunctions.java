/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.core.functions.GenericFunction;
import com.wynntils.core.functions.arguments.FunctionArguments;
import java.util.List;

public class ConditionalFunctions {
    public static class IfStringFunction extends GenericFunction<Object> {
        @Override
        public Object getValue(FunctionArguments arguments) {
            if (arguments.getArgument("condition").getBooleanValue()) {
                return arguments.getArgument("ifTrue").getValue();
            } else {
                return arguments.getArgument("ifFalse").getValue();
            }
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("condition", Boolean.class, null),
                    new FunctionArguments.Argument<>("ifTrue", String.class, null),
                    new FunctionArguments.Argument<>("ifFalse", String.class, null)));
        }

        @Override
        public List<String> getAliases() {
            return List.of("if_str");
        }
    }

    public static class IfNumberFunction extends GenericFunction<Object> {
        @Override
        public Object getValue(FunctionArguments arguments) {
            if (arguments.getArgument("condition").getBooleanValue()) {
                return arguments.getArgument("ifTrue").getValue();
            } else {
                return arguments.getArgument("ifFalse").getValue();
            }
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("condition", Boolean.class, null),
                    new FunctionArguments.Argument<>("ifTrue", Number.class, null),
                    new FunctionArguments.Argument<>("ifFalse", Number.class, null)));
        }

        @Override
        public List<String> getAliases() {
            return List.of("if_num");
        }
    }
}
