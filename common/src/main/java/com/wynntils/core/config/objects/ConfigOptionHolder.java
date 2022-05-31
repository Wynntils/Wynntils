/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.objects;

import com.wynntils.core.Reference;
import com.wynntils.core.config.properties.ConfigOption;
import java.lang.reflect.Field;
import org.apache.commons.lang3.reflect.FieldUtils;

public class ConfigOptionHolder {
    private final Object parent;
    private final Field optionField;
    private final Class<?> optionType;
    private final ConfigOption metadata;

    private final Object defaultValue;

    public ConfigOptionHolder(Object parent, Field field, ConfigOption metadata) {
        this.parent = parent;
        this.optionField = field;
        this.optionType = field.getType();
        this.metadata = metadata;

        // save default value to enable easy resetting
        this.defaultValue = getValue();
    }

    public Class<?> getType() {
        return optionType;
    }

    public Field getField() {
        return optionField;
    }

    public ConfigOption getMetadata() {
        return metadata;
    }

    public String getOptionJsonName() {
        return optionField.getName();
    }

    public Object getValue() {
        try {
            return FieldUtils.readField(optionField, parent, true);
        } catch (IllegalAccessException e) {
            Reference.LOGGER.error("Unable to get config option " + getOptionJsonName());
            e.printStackTrace();
            return null;
        }
    }

    public void setValue(Object value) {
        try {
            FieldUtils.writeField(optionField, parent, value, true);
        } catch (IllegalAccessException e) {
            Reference.LOGGER.error("Unable to set config option " + getOptionJsonName());
            e.printStackTrace();
        }
    }

    public void reset() {
        setValue(defaultValue);
    }
}
