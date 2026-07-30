/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates.language;

import com.wynntils.templates.functions.FunctionDefinition;
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
                throw new VerificationException(
                        expression, "Undefined function: " + functionExpression.getFunctionName());
            }

            if (!def.isVarArgs() && functionExpression.getArguments().length != def.parameterTypes().length) {
                throw new VerificationException(
                        expression,
                        "Function " + def.name() + " expects " + def.parameterTypes().length + " arguments, but got "
                                + functionExpression.getArguments().length);
            }

            for (int i = 0; i < functionExpression.getArguments().length; i++) {
                Class<?> actual = getType(functionExpression.getArguments()[i]);
                Class<?> expected = def.isVarArgs() ? def.parameterTypes()[0].componentType() :
                        def.parameterTypes()[i];
                if (!canConformTo(actual, expected)) {
                    throw new VerificationException(expression, "Argument \"" + functionExpression.getFunctionDefinition().getParameterName(i) + "\" of function \"" +
                            def.name() + "\" expects type " + expected.getSimpleName() + ", but got " + actual.getSimpleName());
                }
            }

            for (Expression arg : functionExpression.getArguments()) {
                verifyExpression(arg);
            }
        }
    }

    private boolean canConformTo(Class<?> from, Class<?> to) {
        if (from == to) {
            return true;
        }

        if ((from.isArray() || !from.isPrimitive())
                && (to.isArray() || !to.isPrimitive())) {
            return true;
        }

        // Primitive numeric conversions
        if (from == int.class) {
            return to == long.class
                    || to == float.class
                    || to == double.class
                    || !to.isPrimitive();
        }

        if (from == long.class) {
            return to == int.class
                    || to == float.class
                    || to == double.class
                    || !to.isPrimitive();
        }

        if (from == float.class) {
            return to == int.class
                    || to == long.class
                    || to == double.class
                    || !to.isPrimitive();
        }

        if (from == double.class) {
            return to == int.class
                    || to == long.class
                    || to == float.class
                    || !to.isPrimitive();
        }

        return false;
    }

    private Class<?> getType(Expression expression) {
        if (expression instanceof FunctionExpression functionExpression) {
            FunctionDefinition def = functionExpression.getFunctionDefinition();
            if (def == null) {
                throw new VerificationException(
                        expression, "Undefined function: " + functionExpression.getFunctionName());
            }
            return def.returnType();
        } else if (expression instanceof LiteralExpression literalExpression) {
            return literalExpression.getValueType();
        }

        throw new VerificationException(
                expression, "Unknown expression type: " + expression.getClass().getSimpleName());
    }
}
