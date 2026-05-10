package com.wynntils.core.consumers.functions.vm;

import com.wynntils.core.consumers.functions.expressions.Expression;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;

public interface FunctionNode {
    Type emit(MethodVisitor mv, List<Expression> arguments);
}
