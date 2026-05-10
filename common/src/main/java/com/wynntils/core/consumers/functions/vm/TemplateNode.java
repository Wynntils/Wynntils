package com.wynntils.core.consumers.functions.vm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public interface TemplateNode {
    Type emit(MethodVisitor mv);
}
