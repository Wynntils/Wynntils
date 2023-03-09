/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config;

import com.google.gson.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.overlays.DynamicOverlay;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.utils.ReflectionUtils;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.reflect.FieldUtils;

public class OverlayGroupHolder {
    private final Field field;
    private final Feature parent;
    private final int defaultCount;

    private final Class<?> overlayClass;

    public OverlayGroupHolder(Field field, Feature parent, int defaultCount) {
        this.field = field;
        this.parent = parent;
        this.defaultCount = defaultCount;

        Type genericType = this.field.getGenericType();

        if (genericType instanceof ParameterizedType parameterizedType) {
            this.overlayClass =
                    TypeToken.get(parameterizedType.getActualTypeArguments()[0]).getRawType();
        } else {
            throw new IllegalArgumentException("Field " + field.getName() + " is not a list.");
        }
    }

    public String getConfigKey() {
        return parent.getConfigJsonName() + ".groupedOverlay." + field.getName() + ".ids";
    }

    public int getOverlayCount() {
        try {
            return ((List<?>) FieldUtils.readField(field, parent, true)).size();
        } catch (IllegalAccessException e) {
            return defaultCount;
        }
    }

    public List<? extends Overlay> getOverlays() {
        try {
            return ((List<? extends Overlay>) FieldUtils.readField(field, parent, true));
        } catch (IllegalAccessException e) {
            return List.of();
        }
    }

    public int getDefaultCount() {
        return defaultCount;
    }

    public Feature getParent() {
        return parent;
    }

    public String getFieldName() {
        return field.getName();
    }

    public Class<?> getOverlayClass() {
        return overlayClass;
    }

    public void initGroup(List<Integer> ids) {
        try {
            List<Overlay> overlays = (List<Overlay>) ReflectionUtils.createListWithType(overlayClass);

            for (Integer id : ids) {
                overlays.add((Overlay) overlayClass.getConstructor(int.class).newInstance(id));
            }

            FieldUtils.writeField(field, parent, overlays, true);
        } catch (Exception e) {
            WynntilsMod.error("Failed to initialize grouped overlay: " + field.getName(), e);
        }
    }

    public int extendGroup() {
        List<Integer> ids = getOverlays().stream()
                .map(overlay -> ((DynamicOverlay) overlay).getId())
                .collect(Collectors.toList());

        int newId = 1;
        while (ids.contains(newId)) {
            newId++;
        }

        ids.add(newId);

        initGroup(ids);

        return newId;
    }

    public void removeId(int id) {
        List<Integer> ids = getOverlays().stream()
                .map(overlay -> ((DynamicOverlay) overlay).getId())
                .collect(Collectors.toList());

        ids.remove((Integer) id);

        initGroup(ids);
    }
}
