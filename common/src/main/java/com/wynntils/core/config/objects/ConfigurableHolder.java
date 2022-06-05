/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.objects;

import com.wynntils.core.config.properties.ConfigurableInfo;
import java.util.List;

public class ConfigurableHolder {
    private final Class<?> configurableClass;
    private final List<ConfigOptionHolder> options;
    private final ConfigurableInfo metadata;

    public ConfigurableHolder(Class<?> configurableClass, List<ConfigOptionHolder> options, ConfigurableInfo metadata) {
        this.configurableClass = configurableClass;
        this.options = options;
        this.metadata = metadata;
    }

    public List<ConfigOptionHolder> getOptions() {
        return options;
    }

    public ConfigurableInfo getMetadata() {
        return metadata;
    }

    public String getJsonName() {
        return configurableClass.getSimpleName();
    }
}
