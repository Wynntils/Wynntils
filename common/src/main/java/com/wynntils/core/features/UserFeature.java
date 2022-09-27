/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.config.Config;
import com.wynntils.core.config.ConfigHolder;
import java.util.Optional;

/**
 * A feature that is enabled & disabled by the user.
 */
public abstract class UserFeature extends Feature {
    @Config(key = "feature.wynntils.userFeature.userEnabled")
    protected boolean userEnabled = true;

    @Override
    public final void updateConfigOption(ConfigHolder configHolder) {

        // if user toggle was changed, enable/disable feature accordingly
        if (configHolder.getFieldName().equals("userEnabled")) {
            // If this was changed before init, do not try to toggle

            tryUserToggle();
            return;
        }

        // otherwise, trigger regular config update
        onConfigUpdate(configHolder);
    }

    /** Updates the feature's enabled/disabled state to match the user's setting, if necessary */
    public final void tryUserToggle() {
        if (userEnabled) {
            enable();
        } else {
            disable();
        }
    }

    public void setUserEnabled(boolean newState) {
        Optional<ConfigHolder> opt = getConfigOptionFromString("userEnabled");

        if (opt.isEmpty()) {
            WynntilsMod.error("UserEnabled lacking userEnabled config");
        }

        ConfigHolder holder = opt.get();
        holder.setValue(newState);
    }
}
