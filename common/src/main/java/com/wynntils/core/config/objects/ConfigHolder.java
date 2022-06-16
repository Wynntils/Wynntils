/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.objects;

import com.wynntils.core.config.properties.Config;
import com.wynntils.core.features.properties.FeatureInfo;
import java.lang.reflect.Field;

public class ConfigHolder extends StorageHolder {
    // Config metadata is stored here to allow other systems to use StorageHolder without Config annotation but still be
    // able to use the Config system
    private final Config metadata;

    public ConfigHolder(Object parent, Field field, Config metadata) {
        super(
                parent,
                field,
                field.getType(),
                parent.getClass().getAnnotation(FeatureInfo.class).category(),
                metadata.visible());

        this.metadata = metadata;
    }

    public Config getMetadata() {
        return metadata;
    }
}
