/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted;

import com.wynntils.core.components.Manager;
import com.wynntils.utils.type.Pair;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.reflect.FieldUtils;

public class PersistedManager extends Manager {
    public PersistedManager() {
        super(List.of());
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
}
