/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config;

import com.google.common.base.CaseFormat;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.Configurable;
import com.wynntils.core.consumers.features.Translatable;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.utils.EnumUtils;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.client.resources.language.I18n;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

public class ConfigHolder<T> implements Comparable<ConfigHolder<T>> {
    private final Configurable parent;
    private final Config<T> configObj;
    private final String fieldName;
    private final String i18nKey;
    private final boolean visible;
    private final Type valueType;
    private final boolean allowNull;

    private final T defaultValue;

    private boolean userEdited = false;

    public <P extends Configurable & Translatable> ConfigHolder(
            P parent, Config<T> configObj, String fieldName, String i18nKey, boolean visible, Type valueType) {
        this.parent = parent;
        this.configObj = configObj;
        this.fieldName = fieldName;
        this.i18nKey = i18nKey;
        this.visible = visible;
        this.valueType = valueType;

        // save default value to enable easy resetting
        // We have to deep copy the value, so it is guaranteed that we detect changes
        this.defaultValue = Managers.Json.deepCopy(getValue(), valueType);

        this.allowNull = valueType instanceof Class<?> clazz && NullableConfig.class.isAssignableFrom(clazz);
        if (configObj.get() == null && !allowNull) {
            throw new RuntimeException(
                    "Default config value is null in " + parent.getConfigJsonName() + "." + fieldName);
        }
    }

    public Stream<String> getValidLiterals() {
        if (valueType instanceof Class<?> clazz && clazz.isEnum()) {
            return EnumUtils.getEnumConstants(clazz).stream().map(EnumUtils::toJsonFormat);
        }
        if (valueType.equals(Boolean.class)) {
            return Stream.of("true", "false");
        }
        return Stream.of();
    }

    public Type getType() {
        return valueType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Configurable getParent() {
        return parent;
    }

    public String getJsonName() {
        if (parent instanceof Overlay overlay) {
            // "featureName.overlayName.settingName"
            return getDeclaringFeatureNameCamelCase(overlay) + "." + parent.getConfigJsonName() + "." + getFieldName();
        }
        // "featureName.settingName"
        return parent.getConfigJsonName() + "." + getFieldName();
    }

    private String getDeclaringFeatureNameCamelCase(Overlay overlay) {
        String name = overlay.getDeclaringClassName();
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
    }

    private String getI18nKey() {
        return i18nKey;
    }

    public boolean isVisible() {
        return visible;
    }

    public String getDisplayName() {
        if (!getI18nKey().isEmpty()) {
            return I18n.get(getI18nKey() + ".name");
        }
        return ((Translatable) parent).getTranslation(getFieldName() + ".name");
    }

    public String getDescription() {
        if (!getI18nKey().isEmpty()) {
            return I18n.get(getI18nKey() + ".description");
        }
        return ((Translatable) parent).getTranslation(getFieldName() + ".description");
    }

    public T getValue() {
        return configObj.get();
    }

    public String getValueString() {
        if (configObj.get() == null) return "(null)";

        if (isEnum()) {
            return EnumUtils.toNiceString((Enum<?>) this.getValue());
        }

        return configObj.get().toString();
    }

    public boolean isEnum() {
        return valueType instanceof Class<?> clazz && clazz.isEnum();
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public void setValue(T value) {
        if (value == null && !allowNull) {
            WynntilsMod.warn("Trying to set null to config " + getJsonName() + ". Will be replaced by default.");
            reset();
            return;
        }

        configObj.updateConfig(value);
        parent.updateConfigOption(configObj);
        userEdited = true;
    }

    void restoreValue(Object value) {
        setValue((T) value);
    }

    public boolean valueChanged() {
        if (this.userEdited) {
            return true;
        }

        boolean deepEquals = Objects.deepEquals(getValue(), defaultValue);

        if (deepEquals) {
            return false;
        }

        try {
            return !EqualsBuilder.reflectionEquals(getValue(), defaultValue);
        } catch (RuntimeException ignored) {
            // Reflection equals does not always work, use deepEquals instead of assuming no change
            // Since deepEquals is already false when we reach this, we can assume change
            return true;
        }
    }

    public void reset() {
        // deep copy because writeField set's the field to be our default value instance when resetting, making default
        // value change with the field's actual value
        setValue(Managers.Json.deepCopy(defaultValue, this.valueType));
        // reset this flag so option is no longer saved to file
        userEdited = false;
    }

    public <E extends Enum<E>> T tryParseStringValue(String value) {
        if (isEnum()) {
            return (T) EnumUtils.fromJsonFormat((Class<E>) getType(), value);
        }

        try {
            Class<?> wrapped = ClassUtils.primitiveToWrapper(((Class<?>) valueType));
            return (T) wrapped.getConstructor(String.class).newInstance(value);
        } catch (Exception ignored) {
        }

        // couldn't parse value
        return null;
    }

    @Override
    public int compareTo(ConfigHolder other) {
        return getJsonName().compareTo(other.getJsonName());
    }
}
