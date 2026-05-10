/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.core.consumers.functions.GenericFunction;
import com.wynntils.core.consumers.functions.arguments.AnyArgument;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.core.consumers.functions.expressions.Expression;
import com.wynntils.core.consumers.functions.vm.FunctionNode;
import com.wynntils.core.consumers.functions.vm.TemplateCompiler;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.type.CappedValue;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.List;

public class ConditionalFunctions {
    // NOTE: This class' generic type is only used in the superclass's getFunctionType() method.
    private abstract static class IfFunctionBase<T> extends GenericFunction<Object> implements FunctionNode {
        @Override
        public Object getValue(FunctionArguments arguments) {
            if (arguments.getArgument("condition").getBooleanValue()) {
                return arguments.getArgument("ifTrue").getValue();
            } else {
                return arguments.getArgument("ifFalse").getValue();
            }
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> args) {

            Label falseLabel = new Label();
            Label endLabel = new Label();

            args.get(0).emit(mv);
            mv.visitJumpInsn(Opcodes.IFEQ, falseLabel);

            Type trueType = args.get(1).emit(mv);

            mv.visitJumpInsn(Opcodes.GOTO, endLabel);

            mv.visitLabel(falseLabel);

            Type falseType = args.get(2).emit(mv);

            mv.visitLabel(endLabel);

            Type resultType = TemplateCompiler.promote(trueType, falseType);

            TemplateCompiler.emitCastIfNeeded(trueType, resultType, mv);
            TemplateCompiler.emitCastIfNeeded(falseType, resultType, mv);

            return resultType;
        }
    }

    public static class IfFunction extends IfFunctionBase<Object> {
        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("condition", Boolean.class, null),
                    new AnyArgument("ifTrue"),
                    new AnyArgument("ifFalse")));
        }
    }

    public static class IfStringFunction extends IfFunctionBase<String> {
        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("condition", Boolean.class, null),
                    new Argument<>("ifTrue", String.class, null),
                    new Argument<>("ifFalse", String.class, null)));
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
                    new Argument<>("condition", Boolean.class, null),
                    new Argument<>("ifTrue", Number.class, null),
                    new Argument<>("ifFalse", Number.class, null)));
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
                    new Argument<>("condition", Boolean.class, null),
                    new Argument<>("ifTrue", CappedValue.class, null),
                    new Argument<>("ifFalse", CappedValue.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("if_capped", "if_cap");
        }
    }

    public static class IfCustomColorFunction extends IfFunctionBase<CustomColor> {
        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("condition", Boolean.class, null),
                    new Argument<>("ifTrue", CustomColor.class, null),
                    new Argument<>("ifFalse", CustomColor.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("if_color", "if_customcolor");
        }
    }
}
