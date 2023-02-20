/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.functions.expressions;

import com.wynntils.core.components.Managers;
import com.wynntils.core.functions.Function;
import com.wynntils.utils.type.ErrorOr;
import java.util.Optional;

public class FunctionExpression extends Expression {
    private Function function;

    public FunctionExpression(String rawExpression, Function function) {
        super(rawExpression);
        this.function = function;
    }

    @Override
    public ErrorOr<String> calculate() {
        return ErrorOr.of(Managers.Function.getRawValueString(function, ""));
    }

    public static Optional<Expression> tryParse(String rawExpression) {
        return Optional.ofNullable(Managers.Function.forName(rawExpression)
                .map(function -> new FunctionExpression(rawExpression, function))
                .orElse(null));
    }
}
