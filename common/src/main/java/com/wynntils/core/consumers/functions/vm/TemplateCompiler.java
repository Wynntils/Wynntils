package com.wynntils.core.consumers.functions.vm;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.consumers.functions.templates.Template;
import com.wynntils.core.consumers.functions.templates.TemplatePart;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.performance.Profiler;
import org.objectweb.asm.*;

import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class TemplateCompiler {
    private static class ExpandedClassLoader extends ClassLoader {

        public ExpandedClassLoader() {
            super(WynntilsMod.class.getClassLoader());
        }

        public Class<?> define(byte[] bytes) {
            return defineClass(null, bytes, 0, bytes.length);
        }
    }

    public static Map<Template, Supplier<String>> compiledTemplates = new HashMap<>();
    public static ClassWriter CLASS_WRITER;
    private static int methodIndex = 0;

    public static int nextMethodId() {
        return methodIndex++;
    }

    public static Supplier<String> compile(Template template) {
        try {
            methodIndex = 0;
            ClassWriter cw = new ClassWriter(
                    ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS
            );

            CLASS_WRITER = cw;

            cw.visit(
                    Opcodes.V17,
                    Opcodes.ACC_PUBLIC,
                    "CompiledTemplate",
                    null,
                    "java/lang/Object",
                    null
            );

            List<String> generatedMethods = new ArrayList<>();

            int index = 0;

            for (TemplatePart part : template.getParts()) {

                String methodName = "part_" + (index++);
                generatedMethods.add(methodName);

                MethodVisitor mv = cw.visitMethod(
                        Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC,
                        methodName,
                        "()Ljava/lang/String;",
                        null,
                        null
                );

                mv.visitCode();

                part.emit(mv);

                mv.visitInsn(Opcodes.ARETURN);

                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }

            MethodVisitor mv = cw.visitMethod(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    "run",
                    "()Ljava/lang/String;",
                    null,
                    null
            );

            mv.visitCode();

            mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    "java/lang/StringBuilder",
                    "<init>",
                    "()V",
                    false
            );

            for (String method : generatedMethods) {

                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "CompiledTemplate",
                        method,
                        "()Ljava/lang/String;",
                        false
                );

                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/lang/StringBuilder",
                        "append",
                        "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                        false
                );
            }

            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/StringBuilder",
                    "toString",
                    "()Ljava/lang/String;",
                    false
            );

            mv.visitInsn(Opcodes.ARETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();

            cw.visitEnd();

            byte[] bytes = cw.toByteArray();

            try (FileOutputStream fos = new FileOutputStream(
                    "E:/templates/" + template.hashCode() + ".class")) {
                fos.write(bytes);
            }

            Class<?> clazz = new ExpandedClassLoader().define(bytes);

            Method method = clazz.getMethod("run");

            return () -> {
                try {
                    return (String) method.invoke(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "";
            };

        } catch (Exception e) {
            e.printStackTrace();
            return () -> "";
        }
    }

    public static void emitCastToDouble(Type type, MethodVisitor mv) {
        if (type.equals(Type.INT_TYPE)) {
            mv.visitInsn(Opcodes.I2D);
        } else if (type.equals(Type.FLOAT_TYPE)) {
            mv.visitInsn(Opcodes.F2D);
        } else if (type.equals(Type.LONG_TYPE)) {
            mv.visitInsn(Opcodes.L2D);
        } else if (type.equals(Type.DOUBLE_TYPE)) {
            // do nothing
        } else if (type.equals(Type.BOOLEAN_TYPE)) {
            mv.visitInsn(Opcodes.I2D);
        } else {
            throw new IllegalArgumentException("Cannot cast type " + type + " to double");
        }
    }

    public static void emitCastToInt(Type type, MethodVisitor mv) {
        if (type.equals(Type.INT_TYPE)) {
            // do nothing
        } else if (type.equals(Type.FLOAT_TYPE)) {
            mv.visitInsn(Opcodes.F2I);
        } else if (type.equals(Type.LONG_TYPE)) {
            mv.visitInsn(Opcodes.L2I);
        } else if (type.equals(Type.DOUBLE_TYPE)) {
            mv.visitInsn(Opcodes.D2I);
        } else if (type.equals(Type.BOOLEAN_TYPE)) {
            // do nothing
        } else {
            throw new IllegalArgumentException("Cannot cast type " + type + " to int");
        }
    }

    public static void emitCastToLong(Type type, MethodVisitor mv) {
        if (type.equals(Type.INT_TYPE)) {
            mv.visitInsn(Opcodes.I2L);
        } else if (type.equals(Type.FLOAT_TYPE)) {
            mv.visitInsn(Opcodes.F2L);
        } else if (type.equals(Type.LONG_TYPE)) {
            // do nothing
        } else if (type.equals(Type.DOUBLE_TYPE)) {
            mv.visitInsn(Opcodes.D2L);
        } else if (type.equals(Type.BOOLEAN_TYPE)) {
            mv.visitInsn(Opcodes.I2L);
        } else {
            throw new IllegalArgumentException("Cannot cast type " + type + " to long");
        }
    }

    public static void emitCastIfNeeded(Type from, Type to, MethodVisitor mv) {

        if (from.equals(to)) return;

        int fromSort = from.getSort();
        int toSort = to.getSort();

        // primitive widening/narrowing
        if (fromSort == Type.INT_TYPE.getSort() && toSort == Type.DOUBLE_TYPE.getSort()) {
            mv.visitInsn(Opcodes.I2D);
            return;
        }

        if (fromSort == Type.FLOAT_TYPE.getSort() && toSort == Type.DOUBLE_TYPE.getSort()) {
            mv.visitInsn(Opcodes.F2D);
            return;
        }

        if (fromSort == Type.LONG_TYPE.getSort() && toSort == Type.DOUBLE_TYPE.getSort()) {
            mv.visitInsn(Opcodes.L2D);
            return;
        } else {
            mv.visitTypeInsn(Opcodes.CHECKCAST, to.getInternalName());
        }

    }

    public static void emitToString(Type type, MethodVisitor mv) {
        if (type.equals(Type.getType(String.class))) {
            // do nothing
        } else if (type.equals(Type.INT_TYPE)) {
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Integer",
                    "toString",
                    "(I)Ljava/lang/String;",
                    false
            );
        } else if (type.equals(Type.FLOAT_TYPE)) {
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Float",
                    "toString",
                    "(F)Ljava/lang/String;",
                    false
            );
        } else if (type.equals(Type.LONG_TYPE)) {
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Long",
                    "toString",
                    "(J)Ljava/lang/String;",
                    false
            );
        } else if (type.equals(Type.DOUBLE_TYPE)) {
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Double",
                    "toString",
                    "(D)Ljava/lang/String;",
                    false
            );
        } else if (type.equals(Type.BOOLEAN_TYPE)) {
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Boolean",
                    "toString",
                    "(Z)Ljava/lang/String;",
                    false
            );
        } else if (type.equals(Type.getType(StyledText.class))) {
            TemplateCompiler.emitInvokeVirtual(
                    mv,
                    StyledText.class,
                    "getString",
                    String.class);
        } else {
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/Object",
                    "toString",
                    "()Ljava/lang/String;",
                    false
            );
        }
    }
    public static void ensureType(Type actual, Type expected) {
        if (!actual.equals(expected)) {
            throw new IllegalArgumentException("Expected type " + expected + " but got " + actual);
        }
    }

    public static void ensureType(Type actual, Class<?> expected) {
        if (!actual.equals(Type.getType(expected))) {
            throw new IllegalArgumentException("Expected type " + Type.getType(expected) + " but got " + actual);
        }
    }

    public static Type promote(Type a, Type b) {
        if (a == Type.DOUBLE_TYPE || b == Type.DOUBLE_TYPE)
            return Type.DOUBLE_TYPE;

        if (a == Type.FLOAT_TYPE || b == Type.FLOAT_TYPE)
            return Type.FLOAT_TYPE;

        if (a == Type.LONG_TYPE || b == Type.LONG_TYPE)
            return Type.LONG_TYPE;

        if (a == Type.INT_TYPE || b == Type.INT_TYPE)
            return Type.INT_TYPE;

        return Type.getType(Object.class); // fallback
    }


    private static String buildDescriptor(Class<?>[] params, Class<?> returnType) {
        Type[] argTypes = new Type[params.length];

        for (int i = 0; i < params.length; i++) {
            argTypes[i] = Type.getType(params[i]);
        }

        return Type.getMethodDescriptor(Type.getType(returnType), argTypes);
    }

    public static void emitInvokeStatic(MethodVisitor mv,
                                        Class<?> clazz,
                                        String methodName,
                                        Class<?> returnType,
                                        Class<?>... parameterTypes) {

        String owner = Type.getInternalName(clazz);
        String descriptor = buildDescriptor(parameterTypes, returnType);

        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                owner,
                methodName,
                descriptor,
                false
        );
    }

    public static void emitInvokeVirtual(MethodVisitor mv,
                                         Class<?> clazz,
                                         String methodName,
                                         Class<?> returnType,
                                         Class<?>... parameterTypes) {

        String owner = Type.getInternalName(clazz);
        String descriptor = buildDescriptor(parameterTypes, returnType);

        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                owner,
                methodName,
                descriptor,
                false
        );
    }

    public static String run(Template template) {
        try (Profiler.Scope ignored = Profiler.scope("TemplateCompiler::run")) {
            return compiledTemplates.computeIfAbsent(template, TemplateCompiler::compile).get();
        }
    }

    public static void emitCastToObject(Type type, MethodVisitor mv) {

        switch (type.getSort()) {

            case Type.BOOLEAN -> mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Boolean",
                    "valueOf",
                    "(Z)Ljava/lang/Boolean;",
                    false
            );

            case Type.CHAR -> mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Character",
                    "valueOf",
                    "(C)Ljava/lang/Character;",
                    false
            );

            case Type.BYTE -> mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Byte",
                    "valueOf",
                    "(B)Ljava/lang/Byte;",
                    false
            );

            case Type.SHORT -> mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Short",
                    "valueOf",
                    "(S)Ljava/lang/Short;",
                    false
            );

            case Type.INT -> mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Integer",
                    "valueOf",
                    "(I)Ljava/lang/Integer;",
                    false
            );

            case Type.FLOAT -> mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Float",
                    "valueOf",
                    "(F)Ljava/lang/Float;",
                    false
            );

            case Type.LONG -> mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Long",
                    "valueOf",
                    "(J)Ljava/lang/Long;",
                    false
            );

            case Type.DOUBLE -> mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Double",
                    "valueOf",
                    "(D)Ljava/lang/Double;",
                    false
            );

            case Type.VOID -> throw new IllegalStateException(
                    "Cannot cast void to Object"
            );

            default -> {
                // already an object/array
            }
        }
    }

    public static void emitCastFromObject(Type type, MethodVisitor mv) {

        switch (type.getSort()) {

            case Type.BOOLEAN -> {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Boolean");

                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/lang/Boolean",
                        "booleanValue",
                        "()Z",
                        false
                );
            }

            case Type.CHAR -> {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Character");

                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/lang/Character",
                        "charValue",
                        "()C",
                        false
                );
            }

            case Type.BYTE -> {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Byte");

                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/lang/Byte",
                        "byteValue",
                        "()B",
                        false
                );
            }

            case Type.SHORT -> {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Short");

                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/lang/Short",
                        "shortValue",
                        "()S",
                        false
                );
            }

            case Type.INT -> {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");

                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/lang/Integer",
                        "intValue",
                        "()I",
                        false
                );
            }

            case Type.FLOAT -> {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Float");

                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/lang/Float",
                        "floatValue",
                        "()F",
                        false
                );
            }

            case Type.LONG -> {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Long");

                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/lang/Long",
                        "longValue",
                        "()J",
                        false
                );
            }

            case Type.DOUBLE -> {
                mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Double");

                mv.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/lang/Double",
                        "doubleValue",
                        "()D",
                        false
                );
            }

            case Type.VOID -> throw new IllegalStateException(
                    "Cannot cast Object to void"
            );

            default -> {
                mv.visitTypeInsn(
                        Opcodes.CHECKCAST,
                        type.getInternalName()
                );
            }
        }
    }
}
