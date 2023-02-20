/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.functions.expressions.parser;

import com.wynntils.core.functions.expressions.Expression;
import com.wynntils.core.functions.expressions.FunctionExpression;
import com.wynntils.utils.type.ErrorOr;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class ExpressionParser {
    private static final List<Function<String, Optional<Expression>>> registeredExpressions =
            List.of(FunctionExpression::tryParse);

    private ExpressionParser() {}

    public static ErrorOr<Expression> tryParse(String rawExpression) {
        for (Function<String, Optional<Expression>> expression : registeredExpressions) {
            Optional<Expression> optionalExpression = expression.apply(rawExpression);

            if (optionalExpression.isPresent()) {
                return ErrorOr.of(optionalExpression.get());
            }
        }

        return ErrorOr.error("Could not parse expression: \"" + rawExpression + "\"");
    }
}
