/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates.backends.compiler;

import com.wynntils.templates.functions.FunctionDefinition;
import com.wynntils.templates.language.expression.Expression;
import com.wynntils.templates.language.expression.FunctionExpression;
import com.wynntils.templates.language.expression.LiteralExpression;
import com.wynntils.templates.language.parts.TemplateExpressionPart;
import com.wynntils.templates.language.parts.TemplateLiteralPart;
import com.wynntils.templates.language.parts.TemplatePart;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ExpressionEmitter {
    private final CompilerState state;

    public ExpressionEmitter(CompilerState state) {
        this.state = state;
    }

    public void emitParts(List<TemplatePart> parts) throws InvocationTargetException, IllegalAccessException {
        for (TemplatePart part : parts) {
            String methodName = state.getNewPartMethodName();

            MethodVisitor mv = state.getClassWriter()
                    .visitMethod(
                            Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, methodName, "()Ljava/lang/String;", null, null);

            mv.visitCode();

            emitTemplatePart(mv, part);

            mv.visitInsn(Opcodes.ARETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    private void emitTemplatePart(MethodVisitor mv, TemplatePart part)
            throws InvocationTargetException, IllegalAccessException {
        if (part instanceof TemplateLiteralPart literalPart) {
            Type returnType = Type.getType(literalPart.getValue().getClass());
            mv.visitLdcInsn(literalPart.getValue());
            BytecodeOps.emitToString(mv, returnType);
        } else if (part instanceof TemplateExpressionPart expressionPart) {
            Expression folded = ConstantFolder.apply(expressionPart.getExpression());
            Type returnType = emitExpression(mv, folded);
            BytecodeOps.emitToString(mv, returnType);
        }
    }

    private Type emitExpression(MethodVisitor mv, Expression expression) {
        if (expression instanceof LiteralExpression literalExpression) {
            mv.visitLdcInsn(literalExpression.getValue());
            return Type.getType(literalExpression.getValueType());
        } else if (expression instanceof FunctionExpression functionExpression) {
            return emitFunctionExpression(mv, functionExpression);
        }

        return Type.VOID_TYPE;
    }

    private String emitFunctionBody(FunctionExpression functionExpression) {
        String methodName = state.getNewExpressionMethodName();

        FunctionDefinition def = functionExpression.getFunctionDefinition();
        Type returnType = Type.getType(def.returnType());
        Expression[] args = functionExpression.getArguments();
        Method methodToInvoke = def.method();

        MethodVisitor mv = state.getClassWriter()
                .visitMethod(
                        Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC,
                        methodName,
                        "()" + returnType.getDescriptor(),
                        null,
                        null);

        mv.visitCode();

        emitFunctionArguments(mv, def, args);

        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                Type.getInternalName(methodToInvoke.getDeclaringClass()),
                methodToInvoke.getName(),
                Type.getMethodDescriptor(methodToInvoke),
                false);

        BytecodeOps.emitReturn(mv, returnType);

        mv.visitMaxs(0, 0);
        mv.visitEnd();

        return methodName;
    }

    private void emitFunctionArguments(MethodVisitor mv, FunctionDefinition def, Expression[] args) {
        if (def.isVarArgs()) {
            mv.visitIntInsn(Opcodes.BIPUSH, args.length);

            Type varArgType = Type.getType(def.getVarArgType());

            if (def.getVarArgType().isPrimitive()) {
                switch (def.getVarArgType().getName()) {
                    case "int" -> mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
                    case "long" -> mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_LONG);
                    case "float" -> mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_FLOAT);
                    case "double" -> mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_DOUBLE);
                    case "boolean" -> mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN);
                    case "char" -> mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_CHAR);
                    case "byte" -> mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BYTE);
                    case "short" -> mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_SHORT);
                }
            } else {
                mv.visitTypeInsn(Opcodes.ANEWARRAY, varArgType.getInternalName());
            }

            for (int i = 0; i < args.length; i++) {
                mv.visitInsn(Opcodes.DUP);

                mv.visitIntInsn(Opcodes.BIPUSH, i);

                Type actualType = emitExpression(mv, args[i]);

                BytecodeOps.emitCastIfNeeded(mv, actualType, varArgType);

                if (def.getVarArgType().isPrimitive()) {
                    switch (def.getVarArgType().getName()) {
                        case "int", "boolean" -> mv.visitInsn(Opcodes.IASTORE);
                        case "long" -> mv.visitInsn(Opcodes.LASTORE);
                        case "float" -> mv.visitInsn(Opcodes.FASTORE);
                        case "double" -> mv.visitInsn(Opcodes.DASTORE);
                        case "char" -> mv.visitInsn(Opcodes.CASTORE);
                        case "byte" -> mv.visitInsn(Opcodes.BASTORE);
                        case "short" -> mv.visitInsn(Opcodes.SASTORE);
                    }
                } else {
                    mv.visitInsn(Opcodes.AASTORE);
                }
            }
        } else {
            for (int i = 0; i < args.length; i++) {
                Type expectedType = Type.getType(def.parameterTypes()[i]);
                Type actualType = emitExpression(mv, args[i]);
                BytecodeOps.emitCastIfNeeded(mv, actualType, expectedType);
            }
        }
    }

    private Type emitFunctionExpression(MethodVisitor mv, FunctionExpression functionExpression) {
        String method = emitFunctionBody(functionExpression);
        Type t = Type.getType(functionExpression.getFunctionDefinition().returnType());

        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "CompiledTemplate", method, "()" + t.getDescriptor(), false);

        return t;
    }
}
