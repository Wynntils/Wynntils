/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config.upfixers;

import com.google.common.base.CaseFormat;
import com.google.gson.JsonObject;
import com.wynntils.core.config.Config;
import java.util.Set;

@FunctionalInterface
public interface ConfigUpfixer {
    boolean apply(JsonObject configObject, Set<Config<?>> configs);

    default String getUpfixerName() {
        return CaseFormat.UPPER_CAMEL
                .to(CaseFormat.LOWER_CAMEL, this.getClass().getSimpleName())
                .replace("Upfixer", "");
    }
}
