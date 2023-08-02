/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.features;

import com.wynntils.core.config.Config;
import com.wynntils.core.persisted.PersistedOwner;
import java.util.List;
import java.util.Optional;

public interface Configurable extends PersistedOwner {
    void updateConfigOption(Config<?> config);

    /** Registers the configurable's config options. Called by ConfigManager when loaded */
    void addConfigOptions(List<Config<?>> options);

    /** Removes a configurable's config options. Used by ConfigManager with Overlay groups */
    void removeConfigOptions(List<Config<?>> options);

    /** Returns all configurable options registered that should be visible to the user */
    List<Config<?>> getVisibleConfigOptions();

    /** Returns all configurable options  that should be visible to the user */
    List<Config<?>> getConfigOptions();

    /** Returns the config option matching the given name, if it exists */
    Optional<Config<?>> getConfigOptionFromString(String name);

    String getConfigJsonName();
}
