/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.wynntils.core.config.Configurable;
import com.wynntils.core.config.properties.ConfigOption;

/**
 * A feature that is enabled & disabled by the user.
 */
public abstract class UserFeature extends Feature implements Configurable {
    @ConfigOption(displayName = "Enabled", description = "Should this feature be enabled?")
    protected boolean userEnabled = true;
}
