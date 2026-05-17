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

    private final ExpandedClassLoader classLoader;
    private ClassWriter classWriter;
    private final Map<Template, Supplier<String>> compiledTemplates = new HashMap<>();

    public CompilerBackend(ClassLoader parentClassLoader) {
        classLoader = new ExpandedClassLoader(parentClassLoader);
    }

    public Supplier<String> compile(Template template) {
        try {
            classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

            classWriter.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "CompiledTemplate", null, "java/lang/Object", null);

            List<String> partMethods = new ArrayList<>();

            for (int i = 0; i < template.getParts().size(); i++) {
                String methodName = "part_" + i;
                partMethods.add(methodName);

                MethodVisitor mv = classWriter.visitMethod(
                        Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, methodName, "()Ljava/lang/String;", null, null);

                mv.visitCode();

                emitPart(mv, template.getParts().get(i));

                mv.visitInsn(Opcodes.ARETURN);

                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }

            MethodVisitor mv = classWriter.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "run", "()Ljava/lang/String;", null, null);

            mv.visitCode();

            mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);

            for (String method : partMethods) {
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, "CompiledTemplate", method, "()Ljava/lang/String;", false);

                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/lang/StringBuilder",
                        "append",
                        "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                        false);
            }

            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);

            mv.visitInsn(Opcodes.ARETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();

            classWriter.visitEnd();

            byte[] bytes = classWriter.toByteArray();

            try (FileOutputStream fos = new FileOutputStream("E:/templates/" + template.hashCode() + ".class")) {
                fos.write(bytes);
            }

            Class<?> clazz = classLoader.define(bytes);

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
            throw new RuntimeException(
                    "Unknown template part: " + part.getClass().getSimpleName());
        }
    }

    private Type emitExpression(MethodVisitor mv, Expression expression) {
        if (expression instanceof LiteralExpression literalExpression) {
            mv.visitLdcInsn(literalExpression.getValue());
            return Type.getType(literalExpression.getValueType());
        } else if (expression instanceof FunctionExpression functionExpression) {
            FunctionDefinition def = functionExpression.getFunctionDefinition();

            Expression[] args = functionExpression.getArguments();

            if (def.isVarArgs()) {
                mv.visitIntInsn(Opcodes.BIPUSH, args.length);
                if (def.getVarArgType().isPrimitive()) {
                    mv.visitIntInsn(
                            Opcodes.NEWARRAY,
                            switch (def.getVarArgType().getName()) {
                                case "int" -> Opcodes.T_INT;
                                case "long" -> Opcodes.T_LONG;
                                case "float" -> Opcodes.T_FLOAT;
                                case "double" -> Opcodes.T_DOUBLE;
                                case "boolean" -> Opcodes.T_BOOLEAN;
                                case "char" -> Opcodes.T_CHAR;
                                case "byte" -> Opcodes.T_BYTE;
                                case "short" -> Opcodes.T_SHORT;
                                default ->
                                    throw new RuntimeException("Unsupported vararg type: "
                                            + def.getVarArgType().getName());
                            });
                } else {
                    mv.visitTypeInsn(Opcodes.ANEWARRAY, Type.getInternalName(def.getVarArgType()));
                }

                for (int i = 0; i < args.length; i++) {
                    mv.visitInsn(Opcodes.DUP);

                    mv.visitIntInsn(Opcodes.BIPUSH, i);

                    emitExpression(mv, args[i]);

                    mv.visitInsn(Opcodes.DASTORE);
                }
            } else {
                for (Expression argument : args) {
                    emitExpression(mv, argument);
                }
            }

            Method m = def.method();

            String owner = Type.getInternalName(m.getDeclaringClass());
            String name = m.getName();
            String desc = Type.getMethodDescriptor(m);

            mv.visitMethodInsn(Opcodes.INVOKESTATIC, owner, name, desc, false);

            return Type.getReturnType(m);
        } else {
            throw new RuntimeException(
                    "Unknown expression type: " + expression.getClass().getSimpleName());
        }
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

    private void emitBox(MethodVisitor mv, Type type) {
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

            case Type.VOID -> throw new IllegalStateException("Cannot cast void to Object");

            default -> {
                // already an object/array
            }
        }
    }

    private void emitUnbox(MethodVisitor mv, Type type) {
        switch (type.getSort()) {
            case Type.BOOLEAN ->
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);

            case Type.CHAR ->
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false);

            case Type.BYTE -> mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false);

            case Type.SHORT -> mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S", false);

            case Type.INT -> mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);

            case Type.FLOAT -> mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false);

            case Type.LONG -> mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);

            case Type.DOUBLE ->
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false);

            case Type.VOID -> throw new IllegalStateException("Cannot cast void to Object");

            default -> {}
        }
    }

    private void emitToString(MethodVisitor mv, Type type) {
        emitBox(mv, type);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
    }

    @Override
    public String evaluate(Template template) {
        return compiledTemplates.computeIfAbsent(template, this::compile).get();
    }
}
