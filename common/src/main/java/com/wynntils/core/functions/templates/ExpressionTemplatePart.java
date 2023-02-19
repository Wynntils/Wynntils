/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.functions.templates;

import com.wynntils.core.functions.expressions.Expression;
import com.wynntils.core.functions.expressions.parser.ExpressionParser;
import com.wynntils.core.functions.expressions.parser.ParseErrorOr;

public class ExpressionTemplatePart extends TemplatePart {
    private final String expressionString;

    public ExpressionTemplatePart(String part) {
        super(part);

        if (!this.part.startsWith("{") || !this.part.endsWith("}")) {
            throw new IllegalArgumentException("Expression was not wrapped in curly braces.");
        }

        this.expressionString = this.part.substring(1, this.part.length() - 1);
    }

    @Override
    public String getValue() {
        ParseErrorOr<Expression> parse = ExpressionParser.tryParse(this.expressionString);

        return parse.hasError() ? parse.getError() : parse.getValue().calculate();
    }

    @Override
    public String toString() {
        return "ExpressionTemplatePart{" + "expressionString='" + expressionString + "'}";
    }
}
