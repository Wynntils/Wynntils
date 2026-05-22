/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
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
