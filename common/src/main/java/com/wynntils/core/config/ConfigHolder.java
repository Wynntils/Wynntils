/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config;

import com.google.common.base.CaseFormat;
import com.google.gson.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.Configurable;
import com.wynntils.core.features.Translatable;
import com.wynntils.core.features.overlays.Overlay;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Objects;
import net.minecraft.client.resources.language.I18n;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.reflect.FieldUtils;

public class ConfigHolder {
    private final Configurable parent;
    private final Field field;
    private final Type fieldType;

    private final Config metadata;

    private final Object defaultValue;

    private boolean userEdited = false;

    public ConfigHolder(Configurable parent, Field field, Config metadata, Type typeOverride) {
        if (!(parent instanceof Translatable)) {
            throw new RuntimeException("Parent must implement Translatable interface.");
        }

        this.parent = parent;
        this.field = field;
        this.metadata = metadata;

        // This is done so the last subclass gets saved (so tryParseStringValue) works
        // TODO: This is still not perfect. If the config field is an abstract class,
        //       and is not instantiated by default, we cannot get it's actual class easily,
        //       making tryParseStringValue fail.
        //       Use TypeOverride to fix this
        this.fieldType = calculateType(typeOverride, getValue(), field);

        // save default value to enable easy resetting
        // We have to deep copy the value, so it is guaranteed that we detect changes
        this.defaultValue =
                ConfigManager.getGson().fromJson(ConfigManager.getGson().toJson(getValue()), this.fieldType);
    }

    private Type calculateType(Type typeOverride, Object value, Field field) {
        if (typeOverride != null) {
            return typeOverride;
        }

        if (value != null) {
            return value.getClass();
        }

        return field.getType();
    }

    public Type getType() {
        return fieldType;
    }

    public Class<?> getClassOfConfigField() {
        return TypeToken.get(this.getType()).getRawType();
    }

    public Field getField() {
        return field;
    }

    public String getFieldName() {
        return field.getName();
    }

    public Configurable getParent() {
        return parent;
    }

    public String getJsonName() {
        if (parent instanceof Overlay) {
            // "featureName.overlayName.settingName"
            return getDeclaringFeatureNameCamelCase() + "." + parent.getConfigJsonName() + "." + field.getName();
        }
        // "featureName.settingName"
        return parent.getConfigJsonName() + "." + field.getName();
    }

    private String getNameCamelCase() {
        String name = parent.getClass().getSimpleName();
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
    }

    private String getDeclaringFeatureNameCamelCase() {
        String name = parent.getClass().getDeclaringClass().getSimpleName();
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
    }

    public Config getMetadata() {
        return metadata;
    }

    public String getDisplayName() {
        if (!getMetadata().key().isEmpty()) {
            return I18n.get(getMetadata().key() + ".name");
        }
        return ((Translatable) parent).getTranslation(field.getName() + ".name");
    }

    public String getDescription() {
        if (!getMetadata().key().isEmpty()) {
            return I18n.get(getMetadata().key() + ".description");
        }
        return ((Translatable) parent).getTranslation(field.getName() + ".description");
    }

    public Object getValue() {
        try {
            return FieldUtils.readField(field, parent, true);
        } catch (IllegalAccessException e) {
            WynntilsMod.error("Unable to get field " + getJsonName(), e);
            return null;
        }
    }

    public boolean setValue(Object value) {
        try {
            FieldUtils.writeField(field, parent, value, true);
            parent.updateConfigOption(this);
            userEdited = true;
            return true;
        } catch (IllegalAccessException e) {
            WynntilsMod.error("Unable to set field " + getJsonName(), e);
            return false;
        }
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
        setValue(ConfigManager.getGson().fromJson(ConfigManager.getGson().toJson(defaultValue), this.fieldType));
        // reset this flag so option is no longer saved to file
        userEdited = false;
    }

    public Object tryParseStringValue(String value) {
        try {
            Class<?> wrapped = ClassUtils.primitiveToWrapper(((Class<?>) fieldType));
            if (wrapped.isEnum()) {
                return Enum.valueOf((Class<? extends Enum>) wrapped, value);
            }
            return wrapped.getConstructor(String.class).newInstance(value);
        } catch (Exception ignored) {
        }

        // couldn't parse value
        return null;
    }
}
