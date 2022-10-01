/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.wynntils.core.config.ConfigHolder;
import java.util.List;

public interface Configurable {
    void updateConfigOption(ConfigHolder configHolder);

    void addConfigOptions(List<ConfigHolder> options);

    String getConfigJsonName();
}
