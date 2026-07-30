/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.templates.language.expression;

import com.wynntils.templates.functions.FunctionDefinition;

public class FunctionExpression implements Expression {
    private final String functionName;
    private Expression[] arguments;
    private FunctionDefinition functionDefinition;

    public FunctionExpression(String functionName, Expression[] arguments, FunctionDefinition functionDefinition) {
        this.functionName = functionName;
        this.arguments = arguments;
        this.functionDefinition = functionDefinition;
    }

    public FunctionExpression(String functionName, FunctionDefinition functionDefinition) {
        this(functionName, new Expression[0], functionDefinition);
    }

    public String getFunctionName() {
        return functionName;
    }

    public Expression[] getArguments() {
        return arguments;
    }

    public FunctionDefinition getFunctionDefinition() {
        return functionDefinition;
    }
}
