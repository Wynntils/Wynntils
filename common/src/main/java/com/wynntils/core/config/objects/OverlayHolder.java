/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.objects;

import com.wynntils.core.config.properties.Config;
import com.wynntils.core.features.properties.FeatureInfo;
import java.lang.reflect.Field;

public class OverlayHolder extends StorageHolder {
    private final Config metadata;

    public OverlayHolder(Object parent, Field field, Config metadata) {
        super(
                parent,
                field,
                field.getType(),
                parent.getClass().getAnnotation(FeatureInfo.class) != null
                        ? parent.getClass().getAnnotation(FeatureInfo.class).category()
                        : "",
                false);

        this.metadata = metadata;
    }

    public Config getMetadata() {
        return metadata;
    }

    public boolean isEnabled() {
        // Type casting to allow #isEnabled to be called
        // An overlay holder can only be made from a field with type Overlay
        return ((Overlay) getValue()).isEnabled();
    }

    public void setEnabled(boolean toggled) {
        // Type casting to allow #setEnabled to be called
        // An overlay holder can only be made from a field with type Overlay
        setValue(((Overlay) getValue()).setEnabled(true));
    }
}
