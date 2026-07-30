/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates.backends.compiler;

import com.wynntils.templates.backends.compiler.exceptions.TemplateExecutionException;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public final class BytecodeOps {
    public static void emitReturn(MethodVisitor mv, Type t) {
        if (t == Type.INT_TYPE || t == Type.BOOLEAN_TYPE) {
            mv.visitInsn(Opcodes.IRETURN);
        } else if (t == Type.LONG_TYPE) {
            mv.visitInsn(Opcodes.LRETURN);
        } else if (t == Type.FLOAT_TYPE) {
            mv.visitInsn(Opcodes.FRETURN);
        } else if (t == Type.DOUBLE_TYPE) {
            mv.visitInsn(Opcodes.DRETURN);
        } else if (t == Type.VOID_TYPE) {
            mv.visitInsn(Opcodes.RETURN);
        } else {
            mv.visitInsn(Opcodes.ARETURN);
        }
    }

    public static void emitCastIfNeeded(MethodVisitor mv, Type from, Type to) {
        if (from.equals(to)) return;

        if ((from.getSort() == Type.OBJECT || from.getSort() == Type.ARRAY)
                && (to.getSort() == Type.OBJECT || to.getSort() == Type.ARRAY)) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, to.getInternalName());
            return;
        }

        if (from == Type.INT_TYPE) {
            if (to == Type.LONG_TYPE) mv.visitInsn(Opcodes.I2L);
            else if (to == Type.FLOAT_TYPE) mv.visitInsn(Opcodes.I2F);
            else if (to == Type.DOUBLE_TYPE) mv.visitInsn(Opcodes.I2D);
            else emitBox(mv, from);
        } else if (from == Type.LONG_TYPE) {
            if (to == Type.INT_TYPE) mv.visitInsn(Opcodes.L2I);
            else if (to == Type.FLOAT_TYPE) mv.visitInsn(Opcodes.L2F);
            else if (to == Type.DOUBLE_TYPE) mv.visitInsn(Opcodes.L2D);
            else emitBox(mv, from);
        } else if (from == Type.FLOAT_TYPE) {
            if (to == Type.INT_TYPE) mv.visitInsn(Opcodes.F2I);
            else if (to == Type.LONG_TYPE) mv.visitInsn(Opcodes.F2L);
            else if (to == Type.DOUBLE_TYPE) mv.visitInsn(Opcodes.F2D);
            else emitBox(mv, from);
        } else if (from == Type.DOUBLE_TYPE) {
            if (to == Type.INT_TYPE) mv.visitInsn(Opcodes.D2I);
            else if (to == Type.LONG_TYPE) mv.visitInsn(Opcodes.D2L);
            else if (to == Type.FLOAT_TYPE) mv.visitInsn(Opcodes.D2F);
            else emitBox(mv, from);
        } else {
            throw new TemplateExecutionException("Cannot cast from " + from + " to " + to);
        }
    }

    public static void emitBox(MethodVisitor mv, Type type) {
        switch (type.getSort()) {
            case Type.BOOLEAN ->
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);

            case Type.CHAR ->
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);

            case Type.BYTE ->
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);

            case Type.SHORT ->
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);

            case Type.INT ->
                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);

            case Type.FLOAT ->
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);

            case Type.LONG ->
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);

            case Type.DOUBLE ->
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
        }
    }

    public static void emitToString(MethodVisitor mv, Type type) {
        if (type.equals(Type.getType(String.class))) return;

        emitBox(mv, type);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
    }
}
