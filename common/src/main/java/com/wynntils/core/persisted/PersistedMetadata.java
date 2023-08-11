/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted;

import java.lang.reflect.Type;

public class PersistedMetadata<T> {
    private final PersistedOwner owner;
    private final String fieldName;
    private final Type valueType;
    private final T defaultValue;
    private final String i18nKeyOverride;
    private final boolean allowNull;
    private final String jsonName;

    public PersistedMetadata(
            PersistedOwner owner,
            String fieldName,
            Type valueType,
            T defaultValue,
            String i18nKeyOverride,
            boolean allowNull,
            String jsonName) {
        this.owner = owner;
        this.fieldName = fieldName;
        this.valueType = valueType;
        this.defaultValue = defaultValue;
        this.i18nKeyOverride = i18nKeyOverride;
        this.allowNull = allowNull;
        this.jsonName = jsonName;
    }

    public PersistedOwner getOwner() {
        return owner;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Type getValueType() {
        return valueType;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public String getI18nKeyOverride() {
        return i18nKeyOverride;
    }

    public boolean isAllowNull() {
        return allowNull;
    }

    public String getJsonName() {
        return jsonName;
    }
}
