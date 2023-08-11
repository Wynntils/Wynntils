/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.config;

import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.Configurable;
import com.wynntils.core.consumers.features.Translatable;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.PersistedMetadata;
import com.wynntils.core.persisted.PersistedValue;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.stream.Stream;

public class Config<T> extends PersistedValue<T> {
    private PersistedMetadata<T> persistedMetadata;

    public Config(T value) {
        super(value);
    }

    @Override
    public void touched() {
        Managers.Config.saveConfig();
    }

    @Override
    public void store(T value) {
        this.value = value;
        // For now, do not call touch() on configs
    }

    <P extends Configurable & Translatable> void createConfigHolder(P parent, Field configField, Persisted configInfo) {
        Type valueType = Managers.Json.getJsonValueType(configField);
        String fieldName = configField.getName();

        boolean visible = !(this instanceof HiddenConfig<?>);

        String i18nKey = configInfo.i18nKey();

        persistedMetadata = new PersistedMetadata<>(parent, this, fieldName, i18nKey, visible, valueType);
    }

    public Stream<String> getValidLiterals() {
        return getPersistedMetadata().getValidLiterals();
    }

    public Type getType() {
        return getPersistedMetadata().getType();
    }

    public String getFieldName() {
        return getPersistedMetadata().getFieldName();
    }

    public Configurable getParent() {
        return getPersistedMetadata().getParent();
    }

    public String getJsonName() {
        return getPersistedMetadata().getJsonName();
    }

    public boolean isVisible() {
        return getPersistedMetadata().isVisible();
    }

    public String getDisplayName() {
        return getPersistedMetadata().getDisplayName();
    }

    public String getDescription() {
        return getPersistedMetadata().getDescription();
    }

    public T getValue() {
        return getPersistedMetadata().getValue();
    }

    public String getValueString() {
        return getPersistedMetadata().getValueString();
    }

    public boolean isEnum() {
        return getPersistedMetadata().isEnum();
    }

    public T getDefaultValue() {
        return getPersistedMetadata().getDefaultValue();
    }

    public void setValue(T value) {
        getPersistedMetadata().setValue(value);
    }

    void restoreValue(Object value) {
        getPersistedMetadata().restoreValue(value);
    }

    public boolean valueChanged() {
        return getPersistedMetadata().valueChanged();
    }

    public void reset() {
        getPersistedMetadata().reset();
    }

    public T tryParseStringValue(String value) {
        return getPersistedMetadata().tryParseStringValue(value);
    }

    private PersistedMetadata<T> getPersistedMetadata() {
        return persistedMetadata;
    }
}
