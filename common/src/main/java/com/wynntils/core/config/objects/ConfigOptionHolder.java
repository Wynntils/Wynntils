/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.objects;

import com.wynntils.core.Reference;
import com.wynntils.core.config.properties.ConfigOption;
import java.lang.reflect.Field;

public class ConfigOptionHolder {

    private Field optionField;
    private Class<?> optionType;
    private ConfigOption metadata;

    public ConfigOptionHolder(Field field, Class<?> type, ConfigOption metadata) {
        this.optionField = field;
        this.optionType = type;
        this.metadata = metadata;
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
            return optionField.get(null);
        } catch (IllegalAccessException e) {
            Reference.LOGGER.error("Unable to get config option " + getOptionJsonName());
            e.printStackTrace();
            return null;
        }
    }

    public void setValue(Object value) {
        try {
            optionField.set(null, value);
        } catch (IllegalAccessException e) {
            Reference.LOGGER.error("Unable to set config option " + getOptionJsonName());
            e.printStackTrace();
        }
    }
}
