/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractConfigurable implements Configurable {
    private final List<ConfigHolder> configOptions = new ArrayList<>();

    @Override
    public void addConfigOptions(List<ConfigHolder> options) {
        configOptions.addAll(options);
    }

    @Override
    public final List<ConfigHolder> getConfigOptions() {
        return configOptions;
    }
}
