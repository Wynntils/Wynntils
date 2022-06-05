/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.wynntils.core.config.Configurable;
import com.wynntils.core.config.objects.ConfigOptionHolder;
import com.wynntils.core.config.properties.ConfigOption;
import javax.annotation.OverridingMethodsMustInvokeSuper;

/**
 * A feature that is enabled & disabled by the user.
 */
public abstract class UserFeature extends Feature implements Configurable {
    @ConfigOption(displayName = "Enabled", description = "Should this feature be enabled?")
    protected boolean userEnabled = true;

    /** This handles the user enabling/disabling a feature in-game */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void onConfigUpdate(ConfigOptionHolder option) {
        if (!option.getField().getName().equals("userEnabled")) return;

        if (userEnabled) {
            tryEnable();
        } else {
            tryDisable();
        }
    }
}
