/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.config;

import com.google.gson.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.utils.type.RenderElementType;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.reflect.FieldUtils;

public class OverlayGroupHolder {
    private final Field field;
    private final Feature parent;
    private final int defaultCount;
    private final RenderElementType elementType;

    private final Class<?> overlayClass;

    public OverlayGroupHolder(Field field, Feature parent, RenderElementType elementType, int defaultCount) {
        this.field = field;
        this.parent = parent;
        this.elementType = elementType;
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
        return parent.getJsonName() + ".groupedOverlay." + field.getName() + ".ids";
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

    public RenderElementType getElementType() {
        return elementType;
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

    // Do not call this. Use OverlayManager instead.
    public void initGroup(List<Integer> ids) {
        try {
            List<Overlay> overlays = new ArrayList<>();

            for (Integer id : ids) {
                overlays.add((Overlay) overlayClass.getConstructor(int.class).newInstance(id));
            }

            FieldUtils.writeField(field, parent, overlays, true);
        } catch (Exception e) {
            WynntilsMod.error("Failed to initialize grouped overlay: " + field.getName(), e);
        }
    }
}
