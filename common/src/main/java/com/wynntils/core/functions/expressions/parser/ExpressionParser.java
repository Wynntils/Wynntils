/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.functions.expressions.parser;

import com.wynntils.core.functions.expressions.Expression;
import com.wynntils.core.functions.expressions.FunctionExpression;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class ExpressionParser {
    private static final List<Function<String, Optional<Expression>>> registeredExpressions =
            List.of(FunctionExpression::tryParse);

    private ExpressionParser() {}

    public static ParseErrorOr<Expression> tryParse(String rawExpression) {
        for (Function<String, Optional<Expression>> expression : registeredExpressions) {
            Optional<Expression> optionalExpression = expression.apply(rawExpression);

            if (optionalExpression.isPresent()) {
                return ParseErrorOr.of(optionalExpression.get());
            }
        }

        return ParseErrorOr.error("Could not parse expression: \"" + rawExpression + "\"");
    }
}
