/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.features;

import com.wynntils.core.config.ConfigHolder;
import java.util.List;
import java.util.Optional;

public interface Configurable {
    void updateConfigOption(ConfigHolder configHolder);

    /** Registers the configurable's config options. Called by ConfigManager when loaded */
    void addConfigOptions(List<ConfigHolder> options);

    /** Removes a configurable's config options. Used by ConfigManager with Overlay groups */
    void removeConfigOptions(List<ConfigHolder> options);

    /** Returns all configurable options registered that should be visible to the user */
    List<ConfigHolder> getVisibleConfigOptions();

    /** Returns all configurable options  that should be visible to the user */
    List<ConfigHolder> getConfigOptions();

    /** Returns the config option matching the given name, if it exists */
    Optional<ConfigHolder> getConfigOptionFromString(String name);

    String getConfigJsonName();
}
