/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions.templates;

import com.wynntils.core.consumers.functions.expressions.Expression;
import com.wynntils.core.consumers.functions.expressions.parser.ExpressionParser;
import com.wynntils.core.consumers.functions.vm.TemplateCompiler;
import com.wynntils.utils.performance.Profiler;
import com.wynntils.utils.type.ErrorOr;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class ExpressionTemplatePart extends TemplatePart {
    private final ErrorOr<Expression> expression;

    public ExpressionTemplatePart(String part) {
        super(part);

        if (!this.part.startsWith("{") || !this.part.endsWith("}")) {
            throw new IllegalArgumentException("Expression was not wrapped in curly braces.");
        }

        this.expression = ExpressionParser.tryParse(this.part.substring(1, this.part.length() - 1));
    }

    @Override
    public String getValue() {
        try (Profiler.Scope ignored = Profiler.scope("ExpressionTemplatePart::getValue")) {
            if (expression.hasError()) {
                return expression.getError();
            }

            ErrorOr<String> calculatedValue = expression.getValue().calculateFormattedString();

            if (calculatedValue.hasError()) {
                return calculatedValue.getError();
            }

            return calculatedValue.getValue();
        }
    }

    @Override
    public String toString() {
        return "ExpressionTemplatePart{" + "expressionString='" + expression + "'}";
    }

    @Override
    public Type emit(MethodVisitor mv) {
        if (expression.hasError()) {
            mv.visitLdcInsn(expression.getError());
            return Type.getType(String.class);
        }

        Type rt = expression.getValue().emit(mv);

        TemplateCompiler.emitToString(rt, mv);

        return rt;
    }
}
