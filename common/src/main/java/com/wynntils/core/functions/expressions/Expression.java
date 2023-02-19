/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.functions.expressions;

public abstract class Expression {
    protected final String rawExpression;

    protected Expression(String rawExpression) {
        this.rawExpression = rawExpression;
    }

    public abstract String calculate();
}
