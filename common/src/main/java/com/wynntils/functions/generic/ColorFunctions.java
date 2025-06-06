/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.core.consumers.functions.GenericFunction;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.utils.colors.CustomColor;
import java.util.List;

public final class ColorFunctions {
    public static class FromRgbFunction extends GenericFunction<CustomColor> {
        @Override
        public CustomColor getValue(FunctionArguments arguments) {
            int r = Math.max(Math.min(arguments.getArgument("r").getIntegerValue(), 255), 0);
            int g = Math.max(Math.min(arguments.getArgument("g").getIntegerValue(), 255), 0);
            int b = Math.max(Math.min(arguments.getArgument("b").getIntegerValue(), 255), 0);
            return new CustomColor(r, g, b);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("r", Integer.class, null),
                    new FunctionArguments.Argument<>("g", Integer.class, null),
                    new FunctionArguments.Argument<>("b", Integer.class, null)));
        }
    }

    public static class FromRgbPercentFunction extends GenericFunction<CustomColor> {
        @Override
        public CustomColor getValue(FunctionArguments arguments) {
            float r = Math.max(
                    Math.min(arguments.getArgument("r").getDoubleValue().floatValue(), 1), 0);
            float g = Math.max(
                    Math.min(arguments.getArgument("g").getDoubleValue().floatValue(), 1), 0);
            float b = Math.max(
                    Math.min(arguments.getArgument("b").getDoubleValue().floatValue(), 1), 0);
            return new CustomColor(r, g, b);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("r", Number.class, null),
                    new FunctionArguments.Argument<>("g", Number.class, null),
                    new FunctionArguments.Argument<>("b", Number.class, null)));
        }
    }

    public static class ToHexStringFunction extends GenericFunction<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return arguments.getArgument("color").getColorValue().toHexString();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("color", CustomColor.class, null)));
        }
    }
}
