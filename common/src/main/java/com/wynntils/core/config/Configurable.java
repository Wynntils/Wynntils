/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config;

import com.wynntils.core.config.objects.ConfigOptionHolder;

/** Used to indicate that a class contains config options */
public interface Configurable {
    default void onConfigUpdate(ConfigOptionHolder configOption) {}
}
