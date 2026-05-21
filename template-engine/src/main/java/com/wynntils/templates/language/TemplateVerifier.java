/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates.language;

import com.wynntils.templates.functions.FunctionDefinition;
import com.wynntils.templates.language.exception.LanguageException;
import com.wynntils.templates.language.exception.VerificationException;
import com.wynntils.templates.language.expression.Expression;
import com.wynntils.templates.language.expression.FunctionExpression;
import com.wynntils.templates.language.expression.LiteralExpression;
import com.wynntils.templates.language.parts.TemplateExpressionPart;
import com.wynntils.templates.language.parts.TemplatePart;

class TemplateVerifier {
    public Template verify(Template template) {
        for (TemplatePart part : template.getParts()) {
            if (part instanceof TemplateExpressionPart expressionPart) {
                verifyExpression(expressionPart.getExpression());
            }
        }
        return template;
    }

    private void verifyExpression(Expression expression) {
        if (expression instanceof FunctionExpression functionExpression) {
            FunctionDefinition def = functionExpression.getFunctionDefinition();

            if (def == null) {
                throw new VerificationException(expression, "Undefined function: " + functionExpression.getFunctionName());
            }

            if (!def.isVarArgs() && functionExpression.getArguments().length != def.parameterTypes().length) {
                throw new VerificationException(expression, "Function " + def.name() + " expects " + def.parameterTypes().length + " arguments, but got " + functionExpression.getArguments().length);
            }

//            for (int i = 0; i < functionExpression.getArguments().length; i++) {
//                Class<?> actual = getType(functionExpression.getArguments()[i]);
//                Class<?> expected = def.isVarArgs() ? def.parameterTypes()[0].componentType() : def.parameterTypes()[i];
//                if (!expected.isAssignableFrom(actual) && !canBeSafelyDownCast(expected, actual)) {
//                    throw new VerificationException(expression, "Argument " + (i + 1) + " of function '" + def.name() + "' expects type " + expected.getSimpleName() + ", but got " + actual.getSimpleName());
//                }
//            }

            for (Expression arg : functionExpression.getArguments()) {
                verifyExpression(arg);
            }
        }
    }

    private Class<?> getType(Expression expression) {
        if (expression instanceof FunctionExpression functionExpression) {
            FunctionDefinition def = functionExpression.getFunctionDefinition();
            if (def == null) {
                throw new VerificationException(expression, "Undefined function: " + functionExpression.getFunctionName());
            }
            return def.returnType();
        } else if (expression instanceof LiteralExpression literalExpression) {
            return literalExpression.getValueType();
        }

        throw new VerificationException(expression, "Unknown expression type: " + expression.getClass().getSimpleName());
    }
}
