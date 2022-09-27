/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.wynntils.core.config.ConfigHolder;

/**
 * A feature which can expose config options to the user, but it's state is controlled by the mod.
 * Meant for implementing features that does not have a clear on/off state or if it's state should not be managed by a user.
 *
 * <p>Think twice before using this superclass. You can likely solve your problem without this. Only use in really specific edge cases.
 */
public abstract class StateManagedFeature extends Feature {
    @Override
    public final void updateConfigOption(ConfigHolder configHolder) {
        onConfigUpdate(configHolder);
    }
}
