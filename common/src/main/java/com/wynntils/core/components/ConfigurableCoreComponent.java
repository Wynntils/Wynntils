/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.components;

import com.wynntils.core.consumers.features.Configurable;
import com.wynntils.core.persisted.config.Config;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class ConfigurableCoreComponent extends CoreComponent implements Configurable {
    private final List<Config<?>> configOptions = new ArrayList<>();

    @Override
    public final void addConfigOptions(List<Config<?>> options) {
        configOptions.addAll(options);
    }

    @Override
    public final List<Config<?>> getVisibleConfigOptions() {
        return configOptions.stream().filter(Config::isVisible).collect(Collectors.toList());
    }

    @Override
    public final List<Config<?>> getConfigOptions() {
        return configOptions;
    }

    @Override
    public final Optional<Config<?>> getConfigOptionFromString(String name) {
        return getConfigOptions().stream()
                .filter(c -> c.getFieldName().equals(name))
                .findFirst();
    }

    @Override
    public final void updateConfigOption(Config<?> config) {
        onConfigUpdate(config);
    }

    protected void onConfigUpdate(Config<?> config) {}
}
