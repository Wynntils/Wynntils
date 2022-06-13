/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.objects;

import com.wynntils.core.config.properties.Config;
import com.wynntils.core.features.properties.FeatureInfo;
import java.lang.reflect.Field;

public class ConfigHolder extends StorageHolder {
    private final Config metadata;

    public ConfigHolder(Object parent, Field field, Config metadata) {
        super(
                parent,
                field,
                field.getType(),
                field.getAnnotation(FeatureInfo.class).category(),
                metadata.visible());

        this.metadata = metadata;
        // save default value to enable easy resetting
    }

    public Config getMetadata() {
        return metadata;
    }
}
