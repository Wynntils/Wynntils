/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates.language.expression;

public class LiteralExpression implements Expression {
    private Object value;
    private Class<?> type;

    public LiteralExpression(Object value, Class<?> type) {
        this.value = value;
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public Class<?> getValueType() {
        return type;
    }
}
