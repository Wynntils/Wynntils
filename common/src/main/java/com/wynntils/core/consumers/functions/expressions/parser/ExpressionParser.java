/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions.expressions.parser;

import com.wynntils.core.consumers.functions.expressions.ConstantExpression;
import com.wynntils.core.consumers.functions.expressions.Expression;
import com.wynntils.core.consumers.functions.expressions.FunctionExpression;
import com.wynntils.utils.type.ErrorOr;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class ExpressionParser {
    private static final List<Function<String, ErrorOr<Optional<Expression>>>> registeredExpressions =
            List.of(FunctionExpression::tryParse, ConstantExpression::tryParse);

    private ExpressionParser() {}

    public static ErrorOr<Expression> tryParse(String rawExpression) {
        for (Function<String, ErrorOr<Optional<Expression>>> expression : registeredExpressions) {
            ErrorOr<Optional<Expression>> optionalExpression = expression.apply(rawExpression);

            if (optionalExpression.hasError()) {
                return ErrorOr.error(optionalExpression.getError());
            }

            if (optionalExpression.getValue().isPresent()) {
                return ErrorOr.of(optionalExpression.getValue().get());
            }
        }

        return ErrorOr.error("Could not parse expression: \"" + rawExpression + "\"");
    }
}
