/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.upfixers;

import com.google.common.base.CaseFormat;
import com.google.gson.JsonObject;
import com.wynntils.core.persisted.PersistedValue;
import java.util.Set;

@FunctionalInterface
public interface Upfixer {
    boolean apply(JsonObject configObject, Set<PersistedValue<?>> persisteds);

    default String getUpfixerName() {
        return CaseFormat.UPPER_CAMEL
                .to(CaseFormat.LOWER_CAMEL, this.getClass().getSimpleName())
                .replace("Upfixer", "");
    }
}
