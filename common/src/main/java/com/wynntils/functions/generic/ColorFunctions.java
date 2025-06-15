/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.GenericFunction;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import java.util.List;

public final class ColorFunctions {
    public static class FromRgbFunction extends GenericFunction<CustomColor> {
        @Override
        public CustomColor getValue(FunctionArguments arguments) {
            int r = arguments.getArgument("r").getIntegerValue();
            int g = arguments.getArgument("g").getIntegerValue();
            int b = arguments.getArgument("b").getIntegerValue();
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
            float r = arguments.getArgument("r").getDoubleValue().floatValue();
            float g = arguments.getArgument("g").getDoubleValue().floatValue();
            float b = arguments.getArgument("b").getDoubleValue().floatValue();
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

    public static class HueShiftFunction extends GenericFunction<CustomColor> {
        @Override
        public CustomColor getValue(FunctionArguments arguments) {
            CustomColor color = arguments.getArgument("color").getColorValue();
            float degree = arguments.getArgument("degree").getDoubleValue().floatValue();
            return color.hueShift(degree);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("color", CustomColor.class, null),
                    new FunctionArguments.Argument<>("degree", Number.class, null)));
        }
    }

    public static class SaturationShiftFunction extends GenericFunction<CustomColor> {
        @Override
        public CustomColor getValue(FunctionArguments arguments) {
            CustomColor color = arguments.getArgument("color").getColorValue();
            float degree = arguments.getArgument("degree").getDoubleValue().floatValue();
            return color.saturationShift(degree);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("color", CustomColor.class, null),
                    new FunctionArguments.Argument<>("degree", Number.class, null)));
        }
    }

    public static class BrightnessShiftFunction extends GenericFunction<CustomColor> {
        @Override
        public CustomColor getValue(FunctionArguments arguments) {
            CustomColor color = arguments.getArgument("color").getColorValue();
            float degree = arguments.getArgument("degree").getDoubleValue().floatValue();
            return color.brightnessShift(degree);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("color", CustomColor.class, null),
                    new FunctionArguments.Argument<>("degree", Number.class, null)));
        }
    }

    public static class RainbowShaderFunction extends Function<CustomColor> {
        @Override
        public CustomColor getValue(FunctionArguments arguments) {
            return CommonColors.RAINBOW;
        }
    }

    public static class GradientShaderFunction extends Function<CustomColor> {
        @Override
        public CustomColor getValue(FunctionArguments arguments) {
            return CommonColors.GRADIENT;
        }
    }

    public static class FadeShaderFunction extends Function<CustomColor> {
        @Override
        public CustomColor getValue(FunctionArguments arguments) {
            return CommonColors.FADE;
        }
    }

    public static class BlinkShaderFunction extends Function<CustomColor> {
        @Override
        public CustomColor getValue(FunctionArguments arguments) {
            return CommonColors.BLINK;
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
