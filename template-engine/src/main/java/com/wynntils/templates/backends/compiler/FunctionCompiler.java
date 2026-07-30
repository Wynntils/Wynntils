/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates.backends.compiler;

import com.wynntils.templates.language.Template;
import java.lang.reflect.InvocationTargetException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class FunctionCompiler {
    private final CompilerState state = new CompilerState();
    private final ExpressionEmitter emitter = new ExpressionEmitter(state);

    public byte[] compile(Template template) throws InvocationTargetException, IllegalAccessException {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        state.start(cw);

        cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "CompiledTemplate", null, "java/lang/Object", null);

        emitter.emitParts(template.getParts());

        MethodVisitor mv =
                cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "run", "()Ljava/lang/String;", null, null);

        mv.visitCode();

        mv.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);

        for (String method : state.getPartMethods()) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "CompiledTemplate", method, "()Ljava/lang/String;", false);

            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/StringBuilder",
                    "append",
                    "(Ljava/lang/String;)Ljava/lang/StringBuilder;",
                    false);
        }

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);

        mv.visitInsn(Opcodes.ARETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();

        cw.visitEnd();

        return cw.toByteArray();
    }
}
