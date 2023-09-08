/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.type;

import com.wynntils.core.persisted.PersistedOwner;
import java.lang.reflect.Type;

public record PersistedMetadata<T>(
        PersistedOwner owner,
        String fieldName,
        Type valueType,
        T defaultValue,
        String i18nKeyOverride,
        boolean allowNull,
        String jsonName) {}
