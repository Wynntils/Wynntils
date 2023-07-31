/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions;

import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import net.minecraft.client.resources.language.I18n;

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
    public String getTranslation(String keySuffix) {
        return I18n.get("function.wynntils.generic." + getTranslationKeyName() + "." + keySuffix);
    }
}
