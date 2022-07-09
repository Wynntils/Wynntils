/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.wynntils.core.config.ConfigHolder;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public interface Configurable {
    void updateConfigOption(ConfigHolder configHolder);

    void addConfigOptions(List<ConfigHolder> options);

    default Map<String, Type> getTypeOverrides() {
        return Map.of();
    }
    ;
}
