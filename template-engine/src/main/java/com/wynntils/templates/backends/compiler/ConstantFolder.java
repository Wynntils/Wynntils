/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates.backends.compiler;

import com.wynntils.templates.functions.FunctionDefinition;
import com.wynntils.templates.language.expression.Expression;
import com.wynntils.templates.language.expression.FunctionExpression;
import com.wynntils.templates.language.expression.LiteralExpression;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class ConstantFolder {
    // Tries to fold expressions that are either constant or compile time pure into a single literal
    public static Expression apply(Expression expr) throws InvocationTargetException, IllegalAccessException {
        try {
            if (expr instanceof FunctionExpression functionExpression) {
                if (!functionExpression.getFunctionDefinition().isPure()) return expr;

                List<LiteralExpression> literalArgs = new ArrayList<>();

                for (Expression arg : functionExpression.getArguments()) {
                    Expression opt = apply(arg);

                    if (!(opt instanceof LiteralExpression)) {
                        return expr;
                    }

                    literalArgs.add((LiteralExpression) opt);
                }

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
            }

            return expr;
        } finally {
            return expr;
        }
    }
}
