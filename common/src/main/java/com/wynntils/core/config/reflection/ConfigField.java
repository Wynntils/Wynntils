/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.reflection;

import com.wynntils.core.config.Configurable;
import com.wynntils.core.config.annotations.Setting;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class ConfigField<T> {
    public Field field;
    public Configurable holder;

    public ConfigField(Field field, Configurable holder) {
        this.field = field;
        this.holder = holder;

        field.setAccessible(true);
    }

    public Field getField() {
        return field;
    }

    @SuppressWarnings("unchecked")
    public T getFieldValue() {
        try {
            return (T) field.get(holder);
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format(
                            "Unable to load config field for holder %s and field %s",
                            holder.getClass().getSimpleName(), field.getName()));
        }
    }

    public void setFieldValue(T value) {
        try {
            field.set(holder, value);
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format(
                            "Unable to set config field for holder %s and field %s",
                            holder.getClass().getSimpleName(), field.getName()));
        }
    }

    @SuppressWarnings("unchecked")
    public void reset() {
        try {
            T newInstance = (T) field.get(holder.getClass().getConstructor().newInstance());

            setFieldValue(newInstance);
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format(
                            "Unable to set config field for holder %s and field %s",
                            holder.getClass().getSimpleName(), field.getName()));
        }
    }

    public Configurable getHolder() {
        return holder;
    }

    public String getName() {
        return field.getAnnotation(Setting.class).name();
    }

    public String getDescription() {
        return field.getAnnotation(Setting.class).name();
    }

    public <A extends Annotation> A getAnnotation(Class<A> a) {
        return field.getAnnotation(a);
    }
}
