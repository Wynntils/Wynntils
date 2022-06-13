/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.objects;

import com.wynntils.core.Reference;
import java.lang.reflect.Field;
import org.apache.commons.lang3.reflect.FieldUtils;

public abstract class StorageHolder {
    protected final Object parent;
    protected final Field field;
    protected final Class<?> fieldType;

    protected final String category;
    protected final boolean visible;

    protected final Object defaultValue;

    public StorageHolder(Object parent, Field field, Class<?> fieldType, String category, boolean visible) {
        this.parent = parent;
        this.field = field;
        this.fieldType = fieldType;

        this.category = category;
        this.visible = visible;

        // save default value to enable easy resetting
        this.defaultValue = getValue();
    }

    public Class<?> getType() {
        return fieldType;
    }

    public Field getField() {
        return field;
    }

    public String getJsonName() {
        return field.getName();
    }

    public String getCategory() {
        return category;
    }

    public Object getValue() {
        try {
            return FieldUtils.readField(field, parent, true);
        } catch (IllegalAccessException e) {
            Reference.LOGGER.error("Unable to get field " + getJsonName());
            e.printStackTrace();
            return null;
        }
    }

    public void setValue(Object value) {
        try {
            FieldUtils.writeField(field, parent, value, true);
        } catch (IllegalAccessException e) {
            Reference.LOGGER.error("Unable to set field " + getJsonName());
            e.printStackTrace();
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isDefault() {
        return defaultValue.equals(getValue());
    }

    public void reset() {
        setValue(defaultValue);
    }
}
