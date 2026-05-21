/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates.compiler;

import com.wynntils.templates.functions.FunctionDefinition;
import com.wynntils.templates.language.Template;
import com.wynntils.templates.language.expression.Expression;
import com.wynntils.templates.language.expression.FunctionExpression;
import com.wynntils.templates.language.expression.LiteralExpression;
import com.wynntils.templates.language.parts.TemplateExpressionPart;
import com.wynntils.templates.language.parts.TemplateLiteralPart;
import com.wynntils.templates.language.parts.TemplatePart;

import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class CompilerBackend implements TemplateBackend {
    private static class ExpandedClassLoader extends ClassLoader {
        private ExpandedClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Class<?> define(byte[] bytes) {
            return defineClass(null, bytes, 0, bytes.length);
        }
    }

    private final ClassLoader parentClassLoader;
    private ClassWriter classWriter;
    private final Map<Template, Supplier<String>> compiledTemplates = new HashMap<>();

    public CompilerBackend(ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
    }

    private int expressionCounter = 0;

    public Supplier<String> compile(Template template) {
        try {
            expressionCounter = 0;
            classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

            classWriter.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "CompiledTemplate", null, "java/lang/Object", null);

            List<String> partMethods = new ArrayList<>();

            for (int i = 0; i < template.getParts().size(); i++) {
                String methodName = "part_" + i;
                partMethods.add(methodName);

                MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, methodName, "()Ljava/lang/String;", null, null);

                mv.visitCode();

                emitPart(mv, template.getParts().get(i));

                mv.visitInsn(Opcodes.ARETURN);

                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }

            MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "run", "()Ljava/lang/String;", null, null);

            mv.visitCode();

            mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);

            for (String method : partMethods) {
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "CompiledTemplate", method, "()Ljava/lang/String;", false);

                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            }

            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);

            mv.visitInsn(Opcodes.ARETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();

            classWriter.visitEnd();

            byte[] bytes = classWriter.toByteArray();

            try (FileOutputStream fos = new FileOutputStream("E:/templates/" + template.hashCode() + ".class")) {
                fos.write(bytes);
            }

            Class<?> clazz = new ExpandedClassLoader(parentClassLoader).define(bytes);

            Method method = clazz.getMethod("run");

            // blegh ugly
            return () -> {
                try {
                    return (String) method.invoke(null);
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
            return () -> "";
        }
    }

    private void emitPart(MethodVisitor mv, TemplatePart part) {
        if (part instanceof TemplateLiteralPart literalPart) {
            mv.visitLdcInsn(literalPart.getValue());
        } else if (part instanceof TemplateExpressionPart expressionPart) {
            Type t1 = emitExpression(mv, optimize(expressionPart.getExpression()));
            emitToString(mv, t1);
        } else {
            throw new RuntimeException("Unknown template part: " + part.getClass().getSimpleName());
        }
    }

    private Type emitExpression(MethodVisitor mv, Expression expression) {
        if (expression instanceof LiteralExpression literalExpression) {
            mv.visitLdcInsn(literalExpression.getValue());
            return Type.getType(literalExpression.getValueType());
        } else if (expression instanceof FunctionExpression functionExpression) {
            return emitFunctionExpression(mv, functionExpression);
        } else {
            throw new RuntimeException("Unknown expression type: " + expression.getClass().getSimpleName());
        }
    }

    private String emitFunctionBody(FunctionExpression functionExpression) {
        String methodName = "expr_" + (expressionCounter++);

        FunctionDefinition def = functionExpression.getFunctionDefinition();
        Type returnType = Type.getType(def.returnType());

        MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, methodName, "()" + returnType.getDescriptor(), null, null);

        mv.visitCode();

        Expression[] args = functionExpression.getArguments();

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
                    default -> throw new RuntimeException("Unsupported vararg type: " + def.getVarArgType().getName());
                }
            } else {
                mv.visitTypeInsn(Opcodes.ANEWARRAY, varArgType.getInternalName());
            }

            for (int i = 0; i < args.length; i++) {
                mv.visitInsn(Opcodes.DUP);

                mv.visitIntInsn(Opcodes.BIPUSH, i);

                Type actualType = emitExpression(mv, args[i]);

                emitCastIfNeeded(mv, actualType, varArgType);

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
                emitCastIfNeeded(mv, actualType, expectedType);
            }

        }

        Method m = def.method();

        String owner = Type.getInternalName(m.getDeclaringClass());
        String name = m.getName();
        String desc = Type.getMethodDescriptor(m);

        mv.visitMethodInsn(Opcodes.INVOKESTATIC, owner, name, desc, false);

        emitReturn(mv, returnType);

        mv.visitMaxs(0, 0);
        mv.visitEnd();

        return methodName;
    }

    private void emitReturn(MethodVisitor mv, Type t) {
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

    private void emitCastIfNeeded(MethodVisitor mv, Type from, Type to) {
        if (from.equals(to)) return;

        if ((from.getSort() == Type.OBJECT || from.getSort() == Type.ARRAY) && (to.getSort() == Type.OBJECT || to.getSort() == Type.ARRAY)) {
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
            throw new RuntimeException("Cannot cast from " + from + " to " + to);
        }
    }

    private Type emitFunctionExpression(MethodVisitor mv, FunctionExpression functionExpression) {
        String method = emitFunctionBody(functionExpression);
        Type t = Type.getType(functionExpression.getFunctionDefinition().returnType());

        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "CompiledTemplate", method, "()" + t.getDescriptor(), false);

        return t;
    }

    private void emitBox(MethodVisitor mv, Type type) {
        switch (type.getSort()) {
            case Type.BOOLEAN ->
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);

            case Type.CHAR ->
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);

            case Type.BYTE ->
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);

            case Type.SHORT ->
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);

            case Type.INT ->
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);

            case Type.FLOAT ->
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);

            case Type.LONG ->
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);

            case Type.DOUBLE ->
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
        }
    }

    private void emitToString(MethodVisitor mv, Type type) {
        if (type.equals(Type.getType(String.class))) return;
        emitBox(mv, type);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
    }


    private Expression optimize(Expression expr) {
        if (expr instanceof FunctionExpression functionExpression) {
            if (!functionExpression.getFunctionDefinition().isPure()) return expr;

            List<LiteralExpression> literalArgs = new ArrayList<>();

            for (Expression arg : functionExpression.getArguments()) {
                Expression opt = optimize(arg);

                if (!(opt instanceof LiteralExpression)) {
                    return expr;
                }

                literalArgs.add((LiteralExpression) opt);
            }

            try {
                FunctionDefinition def = functionExpression.getFunctionDefinition();
                Method m = def.method();

                Object result;

                if (m.isVarArgs()) {
                    Object array = Array.newInstance(literalArgs.getFirst().getValueType(), literalArgs.size());

                    for (int i = 0; i < literalArgs.size(); i++) {
                        Array.set(array, i, literalArgs.get(i).getValue());
                    }

                    result = m.invoke(null, array);

                } else {
                    Object[] values = new Object[literalArgs.size()];

                    for (int i = 0; i < literalArgs.size(); i++) {
                        values[i] = literalArgs.get(i).getValue();
                    }

                    result = m.invoke(null, values);
                }

                return new LiteralExpression(result, def.returnType());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return expr;
    }

    @Override
    public String evaluate(Template template) {
        return compiledTemplates.computeIfAbsent(template, this::compile).get();
    }
}
