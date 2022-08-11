/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.wynntils.core.config.ConfigHolder;

/**
 * A feature controlled from within Wynntils. Meant for implementing functionality that shouldn't be exposed to users.
 */
public abstract class StatelessFeature extends Feature {
    @Override
    public final void updateConfigOption(ConfigHolder configHolder) {
        onConfigUpdate(configHolder);
    }
}
