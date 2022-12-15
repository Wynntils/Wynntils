/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.upfixers;

import com.google.gson.JsonObject;

public interface ConfigUpfixer {
    boolean apply(JsonObject configObject);

    String getUpfixerName();
}
