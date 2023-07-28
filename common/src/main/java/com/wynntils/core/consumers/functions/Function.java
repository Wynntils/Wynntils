/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.functions;

import com.google.common.base.CaseFormat;
import com.wynntils.core.consumers.features.Translatable;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import net.minecraft.client.resources.language.I18n;

public abstract class Function<T> implements Translatable {
    private final String name;
    private final String translationName;

    protected Function() {
        String name = this.getClass().getSimpleName().replace("Function", "");
        this.name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name);
        this.translationName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
    }

    public abstract T getValue(FunctionArguments arguments);

    public FunctionArguments.Builder getArgumentsBuilder() {
        return FunctionArguments.OptionalArgumentBuilder.EMPTY;
    }

    public String getName() {
        return name;
    }

    public List<String> getAliases() {
        return List.of();
    }

    @Override
    public String getTranslatedName() {
        return getTranslation("name");
    }

    public String getDescription() {
        return getTranslation("description");
    }

    protected String getTranslationKeyName() {
        return translationName;
    }

    @Override
    public String getTranslation(String keySuffix) {
        return I18n.get("function.wynntils." + getTranslationKeyName() + "." + keySuffix);
    }

    public String getArgumentDescription(String argumentName) {
        return getTranslation("argument." + argumentName);
    }

    @SuppressWarnings("unchecked")
    public Class<T> getFunctionType() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }
}
