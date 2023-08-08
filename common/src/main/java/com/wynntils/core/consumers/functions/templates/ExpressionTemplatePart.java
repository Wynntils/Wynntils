/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions.templates;

import com.wynntils.core.consumers.functions.expressions.Expression;
import com.wynntils.core.consumers.functions.expressions.parser.ExpressionParser;
import com.wynntils.utils.type.ErrorOr;

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
        if (expression.hasError()) {
            return expression.getError();
        }

        ErrorOr<String> calculatedValue = expression.getValue().calculateFormattedString();

        if (calculatedValue.hasError()) {
            return calculatedValue.getError();
        }

        return calculatedValue.getValue();
    }

    @Override
    public String toString() {
        return "ExpressionTemplatePart{" + "expressionString='" + expression + "'}";
    }
}
