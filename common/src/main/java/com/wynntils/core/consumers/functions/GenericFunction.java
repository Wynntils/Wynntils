/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions;

import com.wynntils.core.consumers.functions.arguments.FunctionArguments;

/**
 * Generic functions are functions that calculate a value, based on their arguments.
 * They differ from {@link Function} in that they do not have any game-related logic.
 *
 * Generic functions should always have required arguments, and should never have optional arguments.
 */
public abstract class GenericFunction<T> extends Function<T> {
    protected abstract FunctionArguments.RequiredArgumentBuilder getRequiredArgumentsBuilder();

    @Override
    public final FunctionArguments.Builder getArgumentsBuilder() {
        return getRequiredArgumentsBuilder();
    }

    @Override
    public String getTranslationKeyName() {
        return "generic." + super.getTranslationKeyName();
    }
}
