/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.config;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.Configurable;
import com.wynntils.core.persisted.PersistedValue;
import com.wynntils.core.persisted.type.PersistedMetadata;
import com.wynntils.utils.EnumUtils;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.client.resources.language.I18n;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

public class Config<T> extends PersistedValue<T> {
    private boolean userEdited = false;

    public Config(T value) {
        super(value);
    }

    @Override
    public void touched() {
        Managers.Config.saveConfig();
    }

    @Override
    public void store(T value) {
        Managers.Persisted.setRaw(this, value);
        // For now, do not call touch() on configs
    }

    // FIXME: Old ways of setting the value. These should be unified, but since
    // they have slightly different semantics, let's do it carfeully step by step.

    public void setValue(T value) {
        if (value == null && !getMetadata().allowNull()) {
            WynntilsMod.warn("Trying to set null to config " + getJsonName() + ". Will be replaced by default.");
            reset();
            return;
        }

        Managers.Persisted.setRaw(this, value);
        ((Configurable) getMetadata().owner()).updateConfigOption(this);
        this.userEdited = true;
    }

    void restoreValue(Object value) {
        setValue((T) value);
    }

    public void reset() {
        T defaultValue = getMetadata().defaultValue();

        // deep copy because writeField set's the field to be our default value instance when resetting, making default
        // value change with the field's actual value
        setValue(Managers.Json.deepCopy(defaultValue, getMetadata().valueType()));
        // reset this flag so option is no longer saved to file
        this.userEdited = false;
    }

    public boolean isVisible() {
        return true;
    }

    public boolean valueChanged() {
        if (this.userEdited) {
            return true;
        }

        // FIXME: I guess at this point the userEdited change would suffice,
        // but check this carefully before removing the old logic below.
        T defaultValue = getMetadata().defaultValue();
        boolean deepEquals = Objects.deepEquals(get(), defaultValue);

        if (deepEquals) {
            return false;
        }

        try {
            return !EqualsBuilder.reflectionEquals(get(), defaultValue);
        } catch (RuntimeException ignored) {
            // Reflection equals does not always work, use deepEquals instead of assuming no change
            // Since deepEquals is already false when we reach this, we can assume change
            return true;
        }
    }

    public String getFieldName() {
        return getMetadata().fieldName();
    }

    public boolean isEnum() {
        return getMetadata().valueType() instanceof Class<?> clazz && clazz.isEnum();
    }

    public T getDefaultValue() {
        return getMetadata().defaultValue();
    }

    public String getDisplayName() {
        return getI18n(".name");
    }

    public String getDescription() {
        return getI18n(".description");
    }

    public Stream<String> getValidLiterals() {
        if (isEnum()) {
            return EnumUtils.getEnumConstants((Class<?>) getType()).stream().map(EnumUtils::toJsonFormat);
        }
        if (getType().equals(Boolean.class)) {
            return Stream.of("true", "false");
        }
        return Stream.of();
    }

    public String getValueString() {
        if (get() == null) return "(null)";

        if (isEnum()) {
            return EnumUtils.toNiceString((Enum<?>) get());
        }

        return get().toString();
    }

    public <E extends Enum<E>> T tryParseStringValue(String value) {
        if (isEnum()) {
            return (T) EnumUtils.fromJsonFormat((Class<E>) getType(), value);
        }

        try {
            Class<?> wrapped = ClassUtils.primitiveToWrapper(((Class<?>) getType()));
            return (T) wrapped.getConstructor(String.class).newInstance(value);
        } catch (Exception ignored) {
        }

        // couldn't parse value
        return null;
    }

    private String getI18n(String suffix) {
        if (!getMetadata().i18nKeyOverride().isEmpty()) {
            return I18n.get(getMetadata().i18nKeyOverride() + suffix);
        }
        return getMetadata().owner().getTranslation(getFieldName() + suffix);
    }

    private PersistedMetadata<T> getMetadata() {
        return Managers.Persisted.getMetadata(this);
    }
}
