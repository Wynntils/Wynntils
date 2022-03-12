/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.reflection;

import com.wynntils.core.config.Configurable;
import com.wynntils.core.config.annotations.Setting;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ConfigReflection {

    public static List<ConfigField<?>> getConfigFields(Configurable configurable) {
        List<ConfigField<?>> configFields = new ArrayList<>();

        for (Class<?> clazz = configurable.getClass();
                Configurable.class.isAssignableFrom(clazz);
                clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                if (field.getAnnotation(Setting.class) != null) {
                    configFields.add(new ConfigField<>(field, configurable));
                }
            }
        }

        return configFields;
    }
}
