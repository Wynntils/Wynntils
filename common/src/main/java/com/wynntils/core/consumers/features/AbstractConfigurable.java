/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.features;

import com.google.common.base.CaseFormat;
import com.wynntils.core.config.ConfigHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractConfigurable implements Configurable {
    private final List<ConfigHolder> configOptions = new ArrayList<>();

    @Override
    public void addConfigOptions(List<ConfigHolder> options) {
        configOptions.addAll(options);
    }

    @Override
    public void removeConfigOptions(List<ConfigHolder> options) {
        configOptions.removeAll(options);
    }

    @Override
    public final List<ConfigHolder> getVisibleConfigOptions() {
        return configOptions.stream().filter(ConfigHolder::isVisible).collect(Collectors.toList());
    }

    @Override
    public final List<ConfigHolder> getConfigOptions() {
        return configOptions;
    }

    @Override
    public final Optional<ConfigHolder> getConfigOptionFromString(String name) {
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
