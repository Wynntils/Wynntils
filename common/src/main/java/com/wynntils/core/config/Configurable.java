/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config;

import com.google.common.base.CaseFormat;
import java.util.List;

public interface Configurable {
    void updateConfigOption(ConfigHolder configHolder);

    /** Registers the configurable's config options. Called by ConfigManager when loaded */
    void addConfigOptions(List<ConfigHolder> options);

    /** Returns all configurable options that should be visible to the user */
    List<ConfigHolder> getConfigOptions();

    default String getShortName() {
        return this.getClass().getSimpleName();
    }

    default String getConfigJsonName() {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, this.getShortName());
    }
}
