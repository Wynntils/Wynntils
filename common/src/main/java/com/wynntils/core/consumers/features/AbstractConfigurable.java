/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.features;

import com.google.common.base.CaseFormat;
import com.wynntils.core.persisted.config.Config;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractConfigurable implements Configurable {
    private final List<Config<?>> configOptions = new ArrayList<>();

    @Override
    public void addConfigOptions(List<Config<?>> options) {
        configOptions.addAll(options);
    }

    @Override
    public void removeConfigOptions(List<Config<?>> options) {
        configOptions.removeAll(options);
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
    public String getConfigJsonName() {
        String name = this.getClass().getSimpleName();
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
    }
}
