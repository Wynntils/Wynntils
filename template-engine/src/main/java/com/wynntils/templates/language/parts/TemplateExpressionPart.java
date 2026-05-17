package com.wynntils.templates.language.parts;

import com.wynntils.templates.language.expression.Expression;

public class TemplateExpressionPart implements TemplatePart {

    private final Expression expression;

    public TemplateExpressionPart(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }
}
