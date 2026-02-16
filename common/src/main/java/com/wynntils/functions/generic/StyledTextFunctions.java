/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.core.consumers.functions.GenericFunction;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.core.consumers.functions.arguments.ListArgument;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CustomColor;
import java.util.List;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

public class StyledTextFunctions {
    public static class StyledTextFunction extends GenericFunction<StyledText> {
        @Override
        public StyledText getValue(FunctionArguments arguments) {
            return StyledText.fromString(arguments.getArgument("value").getStringValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", String.class, null)));
        }
    }

    public static class ConcatStyledTextFunction extends GenericFunction<StyledText> {
        @Override
        public StyledText getValue(FunctionArguments arguments) {
            List<StyledText> values = arguments.getArgument("values").getStyledTextList();
            return StyledText.join("", values);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new ListArgument<>("values", StyledText.class)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("concat_st");
        }
    }

    public static class WithColorFunction extends GenericFunction<StyledText> {
        @Override
        public StyledText getValue(FunctionArguments arguments) {
            StyledText styledText = arguments.getArgument("value").getStyledText();
            CustomColor customColor = arguments.getArgument("color").getColorValue();

            return styledText.map(part -> {
                if (part.getPartStyle().getColor() != CustomColor.NONE) {
                    return part;
                }

                return part.withStyle(style -> style.withColor(customColor));
            });
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", StyledText.class, null), new Argument<>("color", CustomColor.class, null)));
        }
    }

    public static class WithBoldFunction extends GenericFunction<StyledText> {
        @Override
        public StyledText getValue(FunctionArguments arguments) {
            StyledText styledText = arguments.getArgument("value").getStyledText();
            Boolean isBold = arguments.getArgument("isBold").getBooleanValue();

            return styledText.map(part -> part.withStyle(style -> style.withBold(isBold)));
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", StyledText.class, null), new Argument<>("isBold", Boolean.class, null)));
        }
    }

    public static class WithItalicFunction extends GenericFunction<StyledText> {
        @Override
        public StyledText getValue(FunctionArguments arguments) {
            StyledText styledText = arguments.getArgument("value").getStyledText();
            Boolean isItalic = arguments.getArgument("isItalic").getBooleanValue();

            return styledText.map(part -> part.withStyle(style -> style.withItalic(isItalic)));
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", StyledText.class, null), new Argument<>("isItalic", Boolean.class, null)));
        }
    }

    public static class WithStrikethroughFunction extends GenericFunction<StyledText> {
        @Override
        public StyledText getValue(FunctionArguments arguments) {
            StyledText styledText = arguments.getArgument("value").getStyledText();
            Boolean isStrikethrough = arguments.getArgument("isStrikethrough").getBooleanValue();

            return styledText.map(part -> part.withStyle(style -> style.withStrikethrough(isStrikethrough)));
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", StyledText.class, null),
                    new Argument<>("isStrikethrough", Boolean.class, null)));
        }
    }

    public static class WithObfuscatedFunction extends GenericFunction<StyledText> {
        @Override
        public StyledText getValue(FunctionArguments arguments) {
            StyledText styledText = arguments.getArgument("value").getStyledText();
            Boolean isObfuscated = arguments.getArgument("isObfuscated").getBooleanValue();

            return styledText.map(part -> part.withStyle(style -> style.withObfuscated(isObfuscated)));
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", StyledText.class, null),
                    new Argument<>("isObfuscated", Boolean.class, null)));
        }
    }

    public static class WithResourceFontFunction extends GenericFunction<StyledText> {
        @Override
        public StyledText getValue(FunctionArguments arguments) {
            StyledText styledText = arguments.getArgument("value").getStyledText();
            String font = arguments.getArgument("font").getStringValue();
            Identifier fontLocation = Identifier.tryParse(font);

            if (fontLocation == null) return styledText;

            FontDescription fontDescription = new FontDescription.Resource(fontLocation);
            return styledText.map(part -> {
                if (part.getPartStyle().getFont() != FontDescription.DEFAULT) {
                    return part;
                }

                return part.withStyle(style -> style.withFont(fontDescription));
            });
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", StyledText.class, null), new Argument<>("font", String.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("with_font");
        }
    }

    public static class WithShadowColorFunction extends GenericFunction<StyledText> {
        @Override
        public StyledText getValue(FunctionArguments arguments) {
            StyledText styledText = arguments.getArgument("value").getStyledText();
            CustomColor customColor = arguments.getArgument("color").getColorValue();

            return styledText.map(part -> {
                if (part.getPartStyle().getShadowColor() != CustomColor.NONE) {
                    return part;
                }

                return part.withStyle(style -> style.withShadowColor(customColor));
            });
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", StyledText.class, null), new Argument<>("color", CustomColor.class, null)));
        }
    }

    public static class WithUnderlinedFunction extends GenericFunction<StyledText> {
        @Override
        public StyledText getValue(FunctionArguments arguments) {
            StyledText styledText = arguments.getArgument("value").getStyledText();
            Boolean isUnderlined = arguments.getArgument("isUnderlined").getBooleanValue();

            return styledText.map(part -> part.withStyle(style -> style.withUnderlined(isUnderlined)));
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", StyledText.class, null),
                    new Argument<>("isUnderlined", Boolean.class, null)));
        }
    }
}
