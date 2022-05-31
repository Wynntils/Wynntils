/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.objects;

import com.wynntils.core.config.properties.Configurable;
import java.util.List;
import java.util.Locale;

public class ConfigurableHolder {
    private Class<?> configurableClass;
    private List<ConfigOptionHolder> options;
    private Configurable metadata;

    public ConfigurableHolder(Class<?> configurableClass, List<ConfigOptionHolder> options, Configurable metadata) {
        this.configurableClass = configurableClass;
        this.options = options;
        this.metadata = metadata;
    }

    public List<ConfigOptionHolder> getOptions() {
        return options;
    }

    public Configurable getMetadata() {
        return metadata;
    }

    public String getCategoryFileName() {
        return metadata.category().toLowerCase(Locale.ROOT).replace(" ", "_");
    }

    public String getJsonName() {
        return configurableClass.getName();
    }
}
