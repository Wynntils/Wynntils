/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.utils.colors.CustomColor;
import java.util.List;

public class WynnFontFunctions {
    public static class toFancyTextFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            String text = arguments.getArgument("text").getStringValue();
            return Models.WynnFont.toFancyFont(text);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new FunctionArguments.Argument<>("text", String.class, null)));
        }
    }

    public static class toBackgroundTextFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            String text = arguments.getArgument("text").getStringValue();
            CustomColor textColor = arguments.getArgument("textColor").getColorValue();
            CustomColor backgroundColor =
                    arguments.getArgument("backgroundColor").getColorValue();
            String leftEdge = arguments.getArgument("leftEdge").getStringValue();
            String rightEdge = arguments.getArgument("rightEdge").getStringValue();

            return Models.WynnFont.toBackgroundFont(text, textColor, backgroundColor, leftEdge, rightEdge);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new FunctionArguments.Argument<>("text", String.class, null),
                    new FunctionArguments.Argument<>("textColor", CustomColor.class, null),
                    new FunctionArguments.Argument<>("backgroundColor", CustomColor.class, null),
                    new FunctionArguments.Argument<>("leftEdge", String.class, null),
                    new FunctionArguments.Argument<>("rightEdge", String.class, null)));
        }
    }
}
