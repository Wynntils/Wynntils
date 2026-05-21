package com.wynntils.templates.language.exception;

import com.wynntils.templates.language.expression.Expression;

public class VerificationException extends LanguageException {
    private final Expression expression;
    public VerificationException(Expression expression, String message) {
        super(message);
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }
}
