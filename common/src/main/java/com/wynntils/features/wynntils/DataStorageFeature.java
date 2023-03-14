/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.wynntils;

import com.wynntils.core.config.Config;
import com.wynntils.core.config.RegisterConfig;
import com.wynntils.core.features.Feature;

// FIXME: This feature is only needed because we do not have a way to save any data persistently.
//        Remove this when we add persistent data storage other than configs.
public class DataStorageFeature extends Feature {
    public static DataStorageFeature INSTANCE;

    @RegisterConfig(visible = false)
    public final Config<Integer> dryCount = new Config<>(0);

    @RegisterConfig(visible = false)
    public final Config<Integer> dryBoxes = new Config<>(0);
}
