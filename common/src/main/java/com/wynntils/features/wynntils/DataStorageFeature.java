/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.features.wynntils;

import com.wynntils.core.config.ConfigInfo;
import com.wynntils.core.features.StateManagedFeature;

// FIXME: This feature is only needed because we do not have a way to save any data persistently.
//        Remove this when we add persistent data storage other than configs.
public class DataStorageFeature extends StateManagedFeature {
    public static DataStorageFeature INSTANCE;

    @ConfigInfo(visible = false)
    public int dryCount = 0;

    @ConfigInfo(visible = false)
    public int dryBoxes = 0;
}
