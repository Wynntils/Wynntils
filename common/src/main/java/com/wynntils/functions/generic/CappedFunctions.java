/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions.generic;

import com.wynntils.core.consumers.functions.GenericFunction;
import com.wynntils.core.consumers.functions.arguments.Argument;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.core.consumers.functions.expressions.Expression;
import com.wynntils.core.consumers.functions.vm.FunctionNode;
import com.wynntils.core.consumers.functions.vm.TemplateCompiler;
import com.wynntils.utils.type.CappedValue;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.List;

import static com.wynntils.core.consumers.functions.vm.TemplateCompiler.ensureType;

public final class CappedFunctions {
    public static class CurrentFunction extends GenericFunction<Integer> implements FunctionNode {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return arguments.getArgument("capped").getCappedValue().current();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("capped", CappedValue.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("curr");
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            ensureType(arguments.getFirst().emit(mv), CappedValue.class);

            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    Type.getInternalName(CappedValue.class),
                    "current",
                    "()I",
                    false
            );

            return Type.INT_TYPE;
        }
    }

    public static class CapFunction extends GenericFunction<Integer> implements FunctionNode {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return arguments.getArgument("capped").getCappedValue().max();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("capped", CappedValue.class, null)));
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            ensureType(arguments.getFirst().emit(mv), CappedValue.class);

            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    Type.getInternalName(CappedValue.class),
                    "max",
                    "()I",
                    false
            );

            return Type.INT_TYPE;
        }
    }

    public static class RemainingFunction extends GenericFunction<Integer> implements FunctionNode {
        @Override
        public Integer getValue(FunctionArguments arguments) {
            return arguments.getArgument("capped").getCappedValue().getRemaining();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("capped", CappedValue.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("rem");
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            ensureType(arguments.getFirst().emit(mv), CappedValue.class);

            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    Type.getInternalName(CappedValue.class),
                    "getRemaining",
                    "()I",
                    false
            );

            return Type.INT_TYPE;
        }
    }

    public static class PercentageFunction extends GenericFunction<Double> implements FunctionNode {
        @Override
        public Double getValue(FunctionArguments arguments) {
            return arguments.getArgument("capped").getCappedValue().getPercentage();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("capped", CappedValue.class, null)));
        }

        @Override
        protected List<String> getAliases() {
            return List.of("pct");
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            ensureType(arguments.getFirst().emit(mv), CappedValue.class);

            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    Type.getInternalName(CappedValue.class),
                    "getPercentage",
                    "()D",
                    false
            );

            return Type.DOUBLE_TYPE;
        }
    }

    public static class AtCapFunction extends GenericFunction<Boolean> implements FunctionNode {
        @Override
        public Boolean getValue(FunctionArguments arguments) {
            return arguments.getArgument("capped").getCappedValue().isAtCap();
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("capped", CappedValue.class, null)));
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            ensureType(arguments.getFirst().emit(mv), CappedValue.class);

            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    Type.getInternalName(CappedValue.class),
                    "isAtCap",
                    "()Z",
                    false
            );

            return Type.BOOLEAN_TYPE;
        }
    }

    public static class CappedFunction extends GenericFunction<CappedValue> implements FunctionNode {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            return new CappedValue(
                    arguments.getArgument("current").getIntegerValue(),
                    arguments.getArgument("cap").getIntegerValue());
        }

        @Override
        public FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder() {
            return new FunctionArguments.RequiredArgumentBuilder(
                    List.of(new Argument<>("current", Number.class, null), new Argument<>("cap", Number.class, null)));
        }

        @Override
        public Type emit(MethodVisitor mv, List<Expression> arguments) {
            mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(CappedValue.class));
            mv.visitInsn(Opcodes.DUP);

            Type t1 = arguments.get(0).emit(mv);
            TemplateCompiler.emitCastToInt(t1, mv);

            Type t2 = arguments.get(1).emit(mv);
            TemplateCompiler.emitCastToInt(t2, mv);

            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    Type.getInternalName(CappedValue.class),
                    "<init>",
                    "(II)V",
                    false
            );

            return Type.getType(CappedValue.class);
        }
    }
}
