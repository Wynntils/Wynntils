/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions.expressions;

import com.wynntils.core.consumers.functions.vm.TemplateNode;
import com.wynntils.utils.type.ErrorOr;

public abstract class Expression implements TemplateNode {
    private final String rawExpression;

    protected Expression(String rawExpression) {
        this.rawExpression = rawExpression;
    }

    public abstract ErrorOr<Object> calculate();

    public abstract ErrorOr<String> calculateFormattedString();
}
