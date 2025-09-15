/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions;

import com.google.common.base.CaseFormat;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.core.persisted.Translatable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public abstract class Function<T> implements Translatable {
    private final String name;

    private List<String> aliases;

    protected Function() {
        String name = this.getClass().getSimpleName().replace("Function", "");
        this.name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
    }

    @Override
    public String getTypeName() {
        return "Function";
    }

    public abstract T getValue(FunctionArguments arguments);

    public FunctionArguments.Builder getArgumentsBuilder() {
        return FunctionArguments.OptionalArgumentBuilder.EMPTY;
    }

    public String getName() {
        return name;
    }

    protected List<String> getAliases() {
        return List.of();
    }

    public final List<String> getAliasList() {
        // Optimization: we use lazy loading here,
        // because returning a new list every time does a lot of allocations,
        // and the JVM is not interested in optimizing that.
        if (aliases == null) {
            aliases = getAliases();
        }

        return aliases;
    }

    public String getDescription() {
        return getTranslation("description");
    }

    public String getArgumentDescription(String argumentName) {
        return getTranslation("argument." + argumentName);
    }

    public String getReturnTypeName() {
        Type typeArgument = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        if (typeArgument instanceof Class clazz) {
            return clazz.getSimpleName();
        }
        // We assume it is a Class, but keep this as a fallback
        assert false;
        return typeArgument.getTypeName();
    }
}
