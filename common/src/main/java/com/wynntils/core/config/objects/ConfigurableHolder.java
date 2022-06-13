/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.objects;

import com.wynntils.core.config.properties.Configurable;
import java.util.List;

/** Here for reference, to be removed later */
public class ConfigurableHolder {
    private final Class<?> configurableClass;
    private final List<ConfigHolder> options;
    private final Configurable metadata;

    public ConfigurableHolder(Class<?> configurableClass, List<ConfigHolder> options, Configurable metadata) {
        this.configurableClass = configurableClass;
        this.options = options;
        this.metadata = metadata;
    }

    public List<ConfigHolder> getOptions() {
        return options;
    }

    public Configurable getMetadata() {
        return metadata;
    }

    public String getJsonName() {
        return configurableClass.getSimpleName();
    }
}
