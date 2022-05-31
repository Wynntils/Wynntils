/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.objects;

import java.util.ArrayList;
import java.util.List;

public class ConfigCategoryHolder {
    private final String name;
    private final List<ConfigurableHolder> configurables;

    public ConfigCategoryHolder(String name) {
        this.name = name;
        this.configurables = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<ConfigurableHolder> getConfigurables() {
        return configurables;
    }

    public void addConfigurable(ConfigurableHolder configurable) {
        configurables.add(configurable);
    }
}
