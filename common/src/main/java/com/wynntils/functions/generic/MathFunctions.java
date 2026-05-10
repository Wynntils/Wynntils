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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.List;

public final class MathFunctions {
    public static class AddFunction extends GenericFunction<Double> implements FunctionNode {
        @Override
        public Double getValue(FunctionArguments arguments) {
            List<Number> values = arguments.getArgument("values").getNumberList();

            return values.stream().mapToDouble(Number::doubleValue).sum();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new ListArgument<>("values", Number.class)));
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            Type t1 = arguments.getFirst().emit(mv);
            TemplateCompiler.emitCastToDouble(t1, mv);

            for (int i = 1; i < arguments.size(); i++) {
                Type t2 = arguments.get(i).emit(mv);
                TemplateCompiler.emitCastToDouble(t2, mv);
                mv.visitInsn(Opcodes.DADD);
            }

            return Type.DOUBLE_TYPE;
        }
    }

    public static class SubtractFunction extends GenericFunction<Double> implements FunctionNode {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return arguments.getArgument("first").getDoubleValue()
                    - arguments.getArgument("second").getDoubleValue();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("first", Number.class, null), new Argument<>("second", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("sub");
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            Type t1 = arguments.get(0).emit(mv);
            TemplateCompiler.emitCastToDouble(t1, mv);

            Type t2 = arguments.get(1).emit(mv);
            TemplateCompiler.emitCastToDouble(t2, mv);
            mv.visitInsn(Opcodes.DSUB);

            return Type.DOUBLE_TYPE;
        }
    }

    public static class MultiplyFunction extends GenericFunction<Double> implements FunctionNode {
        @Override
        public Double getValue(FunctionArguments arguments) {
            List<Number> values = arguments.getArgument("values").getNumberList();

            return values.stream().mapToDouble(Number::doubleValue).reduce(1, (a, b) -> a * b);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new ListArgument<>("values", Number.class)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("mul");
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            Type t1 = arguments.getFirst().emit(mv);
            TemplateCompiler.emitCastToDouble(t1, mv);

            for (int i = 1; i < arguments.size(); i++) {
                Type t2 = arguments.get(i).emit(mv);
                TemplateCompiler.emitCastToDouble(t2, mv);
                mv.visitInsn(Opcodes.DMUL);
            }

            return Type.DOUBLE_TYPE;
        }
    }

    public static class DivideFunction extends GenericFunction<Double> implements FunctionNode {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return arguments.getArgument("dividend").getDoubleValue()
                    / arguments.getArgument("divisor").getDoubleValue();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("dividend", Number.class, null), new Argument<>("divisor", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("div");
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            Type t1 = arguments.getFirst().emit(mv);
            TemplateCompiler.emitCastToDouble(t1, mv);

            Type t2 = arguments.get(1).emit(mv);
            TemplateCompiler.emitCastToDouble(t2, mv);
            mv.visitInsn(Opcodes.DDIV);

            return Type.DOUBLE_TYPE;
        }
    }

    public static class ModuloFunction extends GenericFunction<Double> implements FunctionNode {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return arguments.getArgument("dividend").getDoubleValue()
                    % arguments.getArgument("divisor").getDoubleValue();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("dividend", Number.class, null), new Argument<>("divisor", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("mod");
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            Type t1 = arguments.get(0).emit(mv);
            TemplateCompiler.emitCastToDouble(t1, mv);

            Type t2 = arguments.get(1).emit(mv);
            TemplateCompiler.emitCastToDouble(t2, mv);
            mv.visitInsn(Opcodes.DREM);

            return Type.DOUBLE_TYPE;
        }
    }

    public static class PowerFunction extends GenericFunction<Double> implements FunctionNode {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Math.pow(
                    arguments.getArgument("base").getDoubleValue(),
                    arguments.getArgument("exponent").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("base", Number.class, null), new Argument<>("exponent", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("pow");
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            Type t1 = arguments.getFirst().emit(mv);
            TemplateCompiler.emitCastToDouble(t1, mv);

            Type t2 = arguments.get(1).emit(mv);
            TemplateCompiler.emitCastToDouble(t2, mv);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Math",
                    "pow",
                    "(DD)D",
                    false
            );

            return Type.DOUBLE_TYPE;
        }
    }

    public static class SquareRootFunction extends GenericFunction<Double> implements FunctionNode {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return Math.sqrt(arguments.getArgument("value").getDoubleValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("sqrt");
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            Type t1 = arguments.getFirst().emit(mv);
            TemplateCompiler.emitCastToDouble(t1, mv);

            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Math",
                    "sqrt",
                    "(D)D",
                    false
            );

            return Type.DOUBLE_TYPE;
        }
    }

    public static class MaxFunction extends GenericFunction<Double> implements FunctionNode {
        @Override
        public Double getValue(FunctionArguments arguments) {
            List<Number> values = arguments.getArgument("values").getNumberList();
            return values.stream().mapToDouble(Number::doubleValue).max().orElse(0);
            // .orElse(0) is safer because max() returns OptionalDouble, but will probably never be used
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new ListArgument<>("values", Number.class)));
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            Type t1 = arguments.getFirst().emit(mv);
            TemplateCompiler.emitCastToDouble(t1, mv);

            for (int i = 1; i < arguments.size(); i++) {
                Type t2 = arguments.get(i).emit(mv);
                TemplateCompiler.emitCastToDouble(t2, mv);

                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "java/lang/Math",
                        "max",
                        "(DD)D",
                        false
                );
            }

            return Type.DOUBLE_TYPE;
        }
    }

    public static class MinFunction extends GenericFunction<Double> implements FunctionNode {
        @Override
        public Double getValue(FunctionArguments arguments) {
            List<Number> values = arguments.getArgument("values").getNumberList();
            return values.stream().mapToDouble(Number::doubleValue).min().orElse(0);
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new ListArgument<>("values", Number.class)));
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            Type t1 = arguments.getFirst().emit(mv);
            TemplateCompiler.emitCastToDouble(t1, mv);

            for (int i = 1; i < arguments.size(); i++) {
                Type t2 = arguments.get(i).emit(mv);
                TemplateCompiler.emitCastToDouble(t2, mv);

                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "java/lang/Math",
                        "min",
                        "(DD)D",
                        false
                );
            }

            return Type.DOUBLE_TYPE;
        }
    }

    public static class RoundFunction extends GenericFunction<Double> implements FunctionNode {
        @Override
        public Double getValue(FunctionArguments arguments) {
            double roundingValue =
                    Math.pow(10, arguments.getArgument("decimals").getIntegerValue());
            return Math.round(arguments.getArgument("value").getDoubleValue() * roundingValue) / roundingValue;
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(
                    new Argument<>("value", Number.class, null), new Argument<>("decimals", Integer.class, null)));
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            mv.visitLdcInsn(10.0);

            Type decimalsType = arguments.get(1).emit(mv);
            TemplateCompiler.emitCastToDouble(decimalsType, mv);

            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Math",
                    "pow",
                    "(DD)D",
                    false
            );

            int roundingLocal = 0;
            mv.visitVarInsn(Opcodes.DSTORE, roundingLocal);

            Type valueType = arguments.get(0).emit(mv);
            TemplateCompiler.emitCastToDouble(valueType, mv);

            mv.visitVarInsn(Opcodes.DLOAD, roundingLocal);
            mv.visitInsn(Opcodes.DMUL);

            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Math",
                    "round",
                    "(D)J",
                    false
            );

            mv.visitInsn(Opcodes.L2D);

            mv.visitVarInsn(Opcodes.DLOAD, roundingLocal);
            mv.visitInsn(Opcodes.DDIV);

            return Type.DOUBLE_TYPE;
        }
    }

    public static class IntegerFunction extends GenericFunction<Integer> implements FunctionNode {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return arguments.getArgument("value").getIntegerValue();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("int");
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            Type t = arguments.getFirst().emit(mv);
            TemplateCompiler.emitCastToInt(t, mv);
            return Type.INT_TYPE;
        }
    }

    public static class LongFunction extends GenericFunction<Long> implements FunctionNode {
        @Override
        public Long getValue(FunctionArguments arguments) {
            return arguments.getArgument("value").getLongValue();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(List.of(new Argument<>("value", Number.class, null)));
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            Type t = arguments.getFirst().emit(mv);
            TemplateCompiler.emitCastToLong(t, mv);
            return Type.LONG_TYPE;
        }
    }

    public static class RandomFunction extends GenericFunction<Double> implements FunctionNode {
        @Override
        public Double getValue(FunctionArguments arguments) {
            double min = arguments.getArgument("min").getIntegerValue();
            double max = arguments.getArgument("max").getIntegerValue();
            return (Math.random() * (max - min)) + min;
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("min", Number.class, null), new Argument<>("max", Number.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("rand");
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            Type t1 = arguments.get(1).emit(mv);
            TemplateCompiler.emitCastToDouble(t1, mv);

            Type t2 = arguments.get(0).emit(mv);
            TemplateCompiler.emitCastToDouble(t2, mv);

            int minValue = 0;
            mv.visitVarInsn(Opcodes.DSTORE, minValue);

            mv.visitVarInsn(Opcodes.DLOAD, minValue);

            mv.visitInsn(Opcodes.DSUB);

            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Math",
                    "random",
                    "()D",
                    false
            );

            mv.visitInsn(Opcodes.DMUL);

            mv.visitVarInsn(Opcodes.DLOAD, minValue);

            mv.visitInsn(Opcodes.DADD);

            return Type.DOUBLE_TYPE;
        }
    }
}
