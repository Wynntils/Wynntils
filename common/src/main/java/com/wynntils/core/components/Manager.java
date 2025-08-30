/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.components;

import com.wynntils.core.properties.Property;
import java.util.List;

/**
 * Managers constitute the core functionality of Wynntils. They provide the framework
 * of the entire mod, but does not have any knowledge about Wynncraft specific details
 * nor any specialized services.
 *
 * Managers are created as singletons in the {@link Managers} holding class.
 */
public abstract class Manager extends CoreComponent {
    protected Manager(List<Manager> dependencies) {
        // dependencies are technically not used, but only required
        // as a reminder for implementers to be wary about dependencies

        // A manager is responsible for never accessing another manager except
        // those listed in the dependencies, due to bootstrapping ordering
    }

    @Override
    public String getTypeName() {
        return "Manager";
    }

    protected final <T> Property<T> createProperty(Class<T> clazz, String propertyPath) {
        return createProperty(clazz, propertyPath, null);
    }

    protected final <T> Property<T> createProperty(Class<T> clazz, String propertyPath, T defaultValue) {
        return new Property<>(this, clazz, propertyPath, defaultValue);
    }
}
