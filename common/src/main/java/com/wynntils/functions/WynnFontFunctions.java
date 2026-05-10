/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.core.consumers.functions.expressions.Expression;
import com.wynntils.core.consumers.functions.vm.FunctionNode;
import com.wynntils.core.consumers.functions.vm.TemplateCompiler;
import com.wynntils.core.text.fonts.WynnFont;
import com.wynntils.utils.colors.CustomColor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;

public class WynnFontFunctions {
    public static class ToFancyTextFunction extends Function<String> implements FunctionNode {
        @Override
        public String getValue(FunctionArguments arguments) {
            String text = arguments.getArgument("text").getStringValue();
            return WynnFont.asFancyFont(text);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("text", String.class, null)));
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {

            Type t1 = arguments.getFirst().emit(mv);
            TemplateCompiler.ensureType(t1, String.class);

            TemplateCompiler.emitInvokeStatic(mv, WynnFont.class, "asFancyFont", String.class, String.class);

            return Type.getType(String.class);
        }
    }

    public static class ToBackgroundTextFunction extends Function<String> implements FunctionNode {
        @Override
        public String getValue(FunctionArguments arguments) {
            String text = arguments.getArgument("text").getStringValue();
            CustomColor textColor = arguments.getArgument("textColor").getColorValue();
            CustomColor backgroundColor =
                    arguments.getArgument("backgroundColor").getColorValue();
            String leftEdge = arguments.getArgument("leftEdge").getStringValue();
            String rightEdge = arguments.getArgument("rightEdge").getStringValue();

            return WynnFont.asBackgroundFont(text, textColor, backgroundColor, leftEdge, rightEdge);
        }

        @Override
        public FunctionArguments.Builder getArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("text", String.class, null),
                    new Argument<>("textColor", CustomColor.class, null),
                    new Argument<>("backgroundColor", CustomColor.class, null),
                    new Argument<>("leftEdge", String.class, null),
                    new Argument<>("rightEdge", String.class, null)));
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            Type t1 = arguments.get(0).emit(mv);
            TemplateCompiler.ensureType(t1, String.class);

            Type t2 = arguments.get(1).emit(mv);
            TemplateCompiler.ensureType(t2, CustomColor.class);

            Type t3 = arguments.get(2).emit(mv);
            TemplateCompiler.ensureType(t3, CustomColor.class);

            Type t4 = arguments.get(3).emit(mv);
            TemplateCompiler.ensureType(t4, String.class);

            Type t5 = arguments.get(4).emit(mv);
            TemplateCompiler.ensureType(t5, String.class);

            TemplateCompiler.emitInvokeStatic(mv, WynnFont.class, "asBackgroundFont", String.class, String.class, CustomColor.class, CustomColor.class, String.class, String.class);

            return Type.getType(String.class);
        }
    }
}
