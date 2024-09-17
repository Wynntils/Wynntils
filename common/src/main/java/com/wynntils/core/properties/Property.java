/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.properties;

import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;

public class Property<T> {
    private final Manager ownerManager;
    private final Class<T> clazz;
    private final String propertyPath;
    private final T defaultValue;

    private boolean loaded;
    private T jvmArgValue;

    public Property(Manager ownerManager, Class<T> clazz, String propertyPath, T defaultValue) {
        this.ownerManager = ownerManager;
        this.clazz = clazz;
        this.propertyPath = propertyPath;
        this.defaultValue = defaultValue;

        // The JVM argument value is lazily loaded
        this.loaded = false;
        this.jvmArgValue = null;
    }

    public T get() {
        if (!loaded) {
            loadJvmArgValue();
        }

        return jvmArgValue != null ? jvmArgValue : defaultValue;
    }

    public Class<T> getClassType() {
        return clazz;
    }

    String getFullJvmArgumentPath() {
        // The JVM argument path is "wynntils.<ownerManagerName>.<propertyPath>"
        return "wynntils." + ownerManager.getTranslationKeyName() + "." + propertyPath;
    }

    private void loadJvmArgValue() {
        jvmArgValue = Managers.SystemProperties.loadJvmArg(this);
        loaded = true;
    }
}
