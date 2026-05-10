/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions.templates;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class LiteralTemplatePart extends TemplatePart {
    public LiteralTemplatePart(String part) {
        super(part);
    }

    @Override
    public String getValue() {
        return part;
    }

    @Override
    public String toString() {
        return "LiteralTemplatePart{" + "part='" + part + "'}";
    }

    @Override
    public Type emit(MethodVisitor mv) {
        mv.visitLdcInsn(part);
        return Type.getType(String.class);
    }
}
