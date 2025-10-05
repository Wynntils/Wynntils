/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted;

import com.google.common.base.CaseFormat;
import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.NullableConfig;
import com.wynntils.core.persisted.type.PersistedMetadata;
import com.wynntils.utils.type.Pair;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang3.reflect.FieldUtils;

public final class PersistedManager extends Manager {
    private final Map<PersistedValue<?>, PersistedMetadata<?>> metadatas = new HashMap<>();
    private final Set<PersistedValue<?>> persisteds = new TreeSet<>();

    public PersistedManager() {
        super(List.of());
    }

    public <T> void setRaw(PersistedValue<T> persisted, Object value) {
        // Hack to allow Config/Storage manager to get around package limitations
        // Will be removed when refactoring is done
        persisted.setWithoutTouch((T) value);
    }

    public void registerOwner(PersistedOwner owner) {
        verifyAnnotations(owner);

        Map<PersistedValue<?>, PersistedMetadata<?>> newMetadatas = new HashMap<>();

        getPersisted(owner, Config.class).forEach(p -> {
            Field configField = p.a();
            Config<?> configObj;
            try {
                configObj = (Config<?>) FieldUtils.readField(configField, owner, true);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot read persisted field: " + configField, e);
            }

            PersistedMetadata<?> metadata = createMetadata((PersistedValue<?>) configObj, owner, configField, p.b());
            newMetadatas.put(configObj, metadata);
        });

        metadatas.putAll(newMetadatas);
        persisteds.addAll(newMetadatas.keySet());
    }

    public List<Pair<Field, Persisted>> getPersisted(PersistedOwner owner, Class<? extends PersistedValue> clazzType) {
        // Get pairs of field and annotation for all persisted values of the requested type
        return Arrays.stream(FieldUtils.getFieldsWithAnnotation(owner.getClass(), Persisted.class))
                .filter(field -> clazzType.isAssignableFrom(field.getType()))
                .map(field -> Pair.of(field, field.getAnnotation(Persisted.class)))
                .toList();
    }

    public void verifyAnnotations(PersistedOwner owner) {
        // Verify that only persistable fields are annotated
        Arrays.stream(FieldUtils.getFieldsWithAnnotation(owner.getClass(), Persisted.class))
                .forEach(field -> {
                    if (!PersistedValue.class.isAssignableFrom(field.getType())) {
                        throw new RuntimeException(
                                "A non-persistable class was marked with @Persisted annotation: " + field);
                    }
                });

        // Verify that we have not missed to annotate a persistable field
        FieldUtils.getAllFieldsList(owner.getClass()).stream()
                .filter(field -> PersistedValue.class.isAssignableFrom(field.getType()))
                .forEach(field -> {
                    Persisted annotation = field.getAnnotation(Persisted.class);
                    if (annotation == null) {
                        throw new RuntimeException("A persisted datatype is missing @Persisted annotation:" + field);
                    }
                });
    }

    public <T> PersistedMetadata<T> getMetadata(PersistedValue<T> persisted) {
        return (PersistedMetadata<T>) metadatas.get(persisted);
    }

    private <T> PersistedMetadata<T> createMetadata(
            PersistedValue<T> persisted, PersistedOwner owner, Field configField, Persisted annotation) {
        Type valueType = Managers.Json.getJsonValueType(configField);
        String fieldName = configField.getName();

        String i18nKeyOverride = annotation.i18nKey();

        // save default value to enable easy resetting
        // We have to deep copy the value, so it is guaranteed that we detect changes
        T defaultValue = Managers.Json.deepCopy(persisted.get(), valueType);

        boolean allowNull = valueType instanceof Class<?> clazz && NullableConfig.class.isAssignableFrom(clazz);
        if (defaultValue == null && !allowNull) {
            throw new RuntimeException("Default config value is null in " + owner.getJsonName() + "." + fieldName);
        }

        String jsonName = getPrefix(owner) + owner.getJsonName() + "." + fieldName;

        return new PersistedMetadata<>(owner, fieldName, valueType, defaultValue, i18nKeyOverride, allowNull, jsonName);
    }

    private String getPrefix(PersistedOwner owner) {
        // "featureName.overlayName.settingName" vs "featureName.settingName"
        if (!(owner instanceof Overlay overlay)) return "";

        String name = overlay.getDeclaringFeatureClassName();
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name) + ".";
    }
}
