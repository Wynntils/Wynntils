/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.core.consumers.functions.GenericFunction;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.core.consumers.functions.arguments.ListArgument;
import com.wynntils.core.consumers.functions.expressions.Expression;
import com.wynntils.core.consumers.functions.vm.FunctionNode;
import com.wynntils.core.consumers.functions.vm.TemplateCompiler;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.Objects;

public class LogicFunctions {
    public static class EqualsFunction extends GenericFunction<Boolean> implements FunctionNode {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return Objects.equals(
                    arguments.getArgument("first").getDoubleValue(),
                    arguments.getArgument("second").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("first", Number.class, null), new Argument<>("second", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("eq");
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            Type t1 = arguments.get(0).emit(mv);
            TemplateCompiler.emitCastToDouble(t1, mv);

            Type t2 = arguments.get(1).emit(mv);
            TemplateCompiler.emitCastToDouble(t2, mv);

            mv.visitInsn(Opcodes.DCMPL);

            Label trueLabel = new Label();
            Label endLabel = new Label();

            mv.visitJumpInsn(Opcodes.IFEQ, trueLabel);

            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitJumpInsn(Opcodes.GOTO, endLabel);

            mv.visitLabel(trueLabel);
            mv.visitInsn(Opcodes.ICONST_1);

            mv.visitLabel(endLabel);

            return Type.BOOLEAN_TYPE;
        }
    }

    public static class NotEqualsFunction extends GenericFunction<Boolean> implements FunctionNode {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return !Objects.equals(
                    arguments.getArgument("first").getDoubleValue(),
                    arguments.getArgument("second").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("first", Number.class, null), new Argument<>("second", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("neq");
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            Type t1 = arguments.get(0).emit(mv);
            TemplateCompiler.emitCastToDouble(t1, mv);

            Type t2 = arguments.get(1).emit(mv);
            TemplateCompiler.emitCastToDouble(t2, mv);

            mv.visitInsn(Opcodes.DCMPL);

            Label trueLabel = new Label();
            Label endLabel = new Label();

            mv.visitJumpInsn(Opcodes.IFEQ, trueLabel);

            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitJumpInsn(Opcodes.GOTO, endLabel);

            mv.visitLabel(trueLabel);
            mv.visitInsn(Opcodes.ICONST_0);

            mv.visitLabel(endLabel);

            return Type.BOOLEAN_TYPE;
        }
    }

    public static class NotFunction extends GenericFunction<Boolean> implements FunctionNode {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return !arguments.getArgument("value").getBooleanValue();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Boolean.class, null)));
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {

            arguments.getFirst().emit(mv);

            Label trueLabel = new Label();
            Label endLabel = new Label();

            mv.visitJumpInsn(Opcodes.IFNE, trueLabel);

            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitJumpInsn(Opcodes.GOTO, endLabel);

            mv.visitLabel(trueLabel);
            mv.visitInsn(Opcodes.ICONST_0);

            mv.visitLabel(endLabel);

            return Type.BOOLEAN_TYPE;
        }
    }

    public static class AndFunction extends GenericFunction<Boolean> implements FunctionNode {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            List<Boolean> values = arguments.getArgument("values").getBooleanList();

            return values.stream().allMatch(Boolean::booleanValue);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new ListArgument<>("values", Boolean.class)));
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            arguments.getFirst().emit(mv);

            for (int i = 1; i < arguments.size(); i++) {
                arguments.get(i).emit(mv);

                mv.visitInsn(Opcodes.IAND);
            }

            return Type.BOOLEAN_TYPE;
        }
    }

    public static class OrFunction extends GenericFunction<Boolean> {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            List<Boolean> values = arguments.getArgument("values").getBooleanList();

            return values.stream().anyMatch(Boolean::booleanValue);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new ListArgument<>("values", Boolean.class)));
        }
    }

    public static class LessThanFunction extends GenericFunction<Boolean> implements FunctionNode {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return arguments.getArgument("first").getDoubleValue()
                    < arguments.getArgument("second").getDoubleValue();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("first", Number.class, null), new Argument<>("second", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("lt");
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {

            Type t1 = arguments.get(0).emit(mv);
            TemplateCompiler.emitCastToDouble(t1, mv);

            Type t2 = arguments.get(1).emit(mv);
            TemplateCompiler.emitCastToDouble(t2, mv);

            mv.visitInsn(Opcodes.DCMPG);

            Label trueLabel = new Label();
            Label endLabel = new Label();

            mv.visitJumpInsn(Opcodes.IFLT, trueLabel);

            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitJumpInsn(Opcodes.GOTO, endLabel);

            mv.visitLabel(trueLabel);
            mv.visitInsn(Opcodes.ICONST_1);

            mv.visitLabel(endLabel);

            return Type.BOOLEAN_TYPE;
        }
    }

    public static class LessThanOrEqualsFunction extends GenericFunction<Boolean> implements FunctionNode {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return arguments.getArgument("first").getDoubleValue()
                    <= arguments.getArgument("second").getDoubleValue();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("first", Number.class, null), new Argument<>("second", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("lte", "less_than_equals", "leq");
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {

            Type t1 = arguments.get(0).emit(mv);
            TemplateCompiler.emitCastToDouble(t1, mv);

            Type t2 = arguments.get(1).emit(mv);
            TemplateCompiler.emitCastToDouble(t2, mv);

            mv.visitInsn(Opcodes.DCMPG);

            Label trueLabel = new Label();
            Label endLabel = new Label();

            mv.visitJumpInsn(Opcodes.IFLE, trueLabel);

            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitJumpInsn(Opcodes.GOTO, endLabel);

            mv.visitLabel(trueLabel);
            mv.visitInsn(Opcodes.ICONST_1);

            mv.visitLabel(endLabel);

            return Type.BOOLEAN_TYPE;
        }
    }

    public static class GreaterThanFunction extends GenericFunction<Boolean> implements FunctionNode {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return arguments.getArgument("first").getDoubleValue()
                    > arguments.getArgument("second").getDoubleValue();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("first", Number.class, null), new Argument<>("second", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("mt", "more_than", "gt");
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {

            Type t1 = arguments.get(0).emit(mv);
            TemplateCompiler.emitCastToDouble(t1, mv);

            Type t2 = arguments.get(1).emit(mv);
            TemplateCompiler.emitCastToDouble(t2, mv);

            mv.visitInsn(Opcodes.DCMPG);

            Label trueLabel = new Label();
            Label endLabel = new Label();

            mv.visitJumpInsn(Opcodes.IFGT, trueLabel);

            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitJumpInsn(Opcodes.GOTO, endLabel);

            mv.visitLabel(trueLabel);
            mv.visitInsn(Opcodes.ICONST_1);

            mv.visitLabel(endLabel);

            return Type.BOOLEAN_TYPE;
        }
    }

    public static class GreaterThanOrEqualsFunction extends GenericFunction<Boolean> implements FunctionNode {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return arguments.getArgument("first").getDoubleValue()
                    >= arguments.getArgument("second").getDoubleValue();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("first", Number.class, null), new Argument<>("second", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("mte", "more_than_equals", "greater_than_equals", "gte", "geq");
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {

            Type t1 = arguments.get(0).emit(mv);
            TemplateCompiler.emitCastToDouble(t1, mv);

            Type t2 = arguments.get(1).emit(mv);
            TemplateCompiler.emitCastToDouble(t2, mv);

            mv.visitInsn(Opcodes.DCMPG);

            Label trueLabel = new Label();
            Label endLabel = new Label();

            mv.visitJumpInsn(Opcodes.IFGE, trueLabel);

            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitJumpInsn(Opcodes.GOTO, endLabel);

            mv.visitLabel(trueLabel);
            mv.visitInsn(Opcodes.ICONST_1);

            mv.visitLabel(endLabel);

            return Type.BOOLEAN_TYPE;
        }
    }
}
