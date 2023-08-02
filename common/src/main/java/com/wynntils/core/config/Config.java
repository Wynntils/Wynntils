/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config;

import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.Configurable;
import com.wynntils.core.consumers.features.Translatable;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.PersistedValue;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.stream.Stream;

public class Config<T> extends PersistedValue<T> implements Comparable<Config<T>> {
    private ConfigHolder<T> configHolder;

    public Config(T value) {
        super(value);
    }

    @Override
    public void touched() {
        Managers.Config.saveConfig();
    }

    public void updateConfig(T value) {
        this.value = value;
    }

    <P extends Configurable & Translatable> void createConfigHolder(P parent, Field configField, Persisted configInfo) {
        Type valueType = Managers.Json.getJsonValueType(configField);
        String fieldName = configField.getName();

        boolean visible = !(this instanceof HiddenConfig<?>);

        String i18nKey = configInfo.i18nKey();

        configHolder = new ConfigHolder<>(parent, this, fieldName, i18nKey, visible, valueType);
    }

    @Override
    public int compareTo(Config<T> other) {
        return configHolder.getJsonName().compareTo(other.getJsonName());
    }

    public Stream<String> getValidLiterals() {
        return configHolder.getValidLiterals();
    }

    public Type getType() {
        return configHolder.getType();
    }

    public String getFieldName() {
        return configHolder.getFieldName();
    }

    public Configurable getParent() {
        return configHolder.getParent();
    }

    public String getJsonName() {
        return configHolder.getJsonName();
    }

    public boolean isVisible() {
        return configHolder.isVisible();
    }

    public String getDisplayName() {
        return configHolder.getDisplayName();
    }

    public String getDescription() {
        return configHolder.getDescription();
    }

    public T getValue() {
        return configHolder.getValue();
    }

    public String getValueString() {
        return configHolder.getValueString();
    }

    public boolean isEnum() {
        return configHolder.isEnum();
    }

    public T getDefaultValue() {
        return configHolder.getDefaultValue();
    }

    public void setValue(T value) {
        configHolder.setValue(value);
    }

    void restoreValue(Object value) {
        configHolder.restoreValue(value);
    }

    public boolean valueChanged() {
        return configHolder.valueChanged();
    }

    public void reset() {
        configHolder.reset();
    }

    public T tryParseStringValue(String value) {
        return configHolder.tryParseStringValue(value);
    }
}
