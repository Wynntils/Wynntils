/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.wynntils.core.config.ConfigHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractConfigurable implements Configurable {
    protected final List<ConfigHolder> configOptions = new ArrayList<>();

    public abstract void updateConfigOption(ConfigHolder configHolder);

    public void addConfigOptions(List<ConfigHolder> options) {
        configOptions.addAll(options);
    }

    public final List<ConfigHolder> getVisibleConfigOptions() {
        return configOptions.stream().filter(c -> c.getMetadata().visible()).collect(Collectors.toList());
    }

    public final List<ConfigHolder> getConfigOptions() {
        return configOptions;
    }

    public final Optional<ConfigHolder> getConfigOptionFromString(String name) {
        return getVisibleConfigOptions().stream()
                .filter(c -> c.getFieldName().equals(name))
                .findFirst();
    }
}
