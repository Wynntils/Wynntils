/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted;

import com.google.common.base.CaseFormat;
import com.wynntils.core.consumers.features.Translatable;

public interface PersistedOwner extends Translatable {
    default String getJsonName() {
        String name = this.getClass().getSimpleName();
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
    }
}
