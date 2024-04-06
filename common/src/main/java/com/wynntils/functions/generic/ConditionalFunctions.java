/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.core.consumers.functions.GenericFunction;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.utils.type.CappedValue;
import java.util.List;

public class ConditionalFunctions {
    // NOTE: This class' generic type is only used in the superclass's getFunctionType() method.
    private abstract static class IfFunctionBase<T> extends GenericFunction<Object> {
        @Override
        public Object getValue(FunctionArguments arguments) {
            if (arguments.getArgument("condition").getBooleanValue()) {
                return arguments.getArgument("ifTrue").getValue();
            } else {
                return arguments.getArgument("ifFalse").getValue();
            }
        }
    }

    public static class IfStringFunction extends IfFunctionBase<String> {
        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("condition", Boolean.class, null),
                    new FunctionArguments.Argument<>("ifTrue", String.class, null),
                    new FunctionArguments.Argument<>("ifFalse", String.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("if_str");
        }
    }

    public static class IfNumberFunction extends IfFunctionBase<Number> {
        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("condition", Boolean.class, null),
                    new FunctionArguments.Argument<>("ifTrue", Number.class, null),
                    new FunctionArguments.Argument<>("ifFalse", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("if_num");
        }
    }

    public static class IfCappedValueFunction extends IfFunctionBase<CappedValue> {
        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("condition", Boolean.class, null),
                    new FunctionArguments.Argument<>("ifTrue", CappedValue.class, null),
                    new FunctionArguments.Argument<>("ifFalse", CappedValue.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("if_capped", "if_cap");
        }
    }
}
