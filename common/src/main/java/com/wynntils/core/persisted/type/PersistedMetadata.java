/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.type;

import com.wynntils.core.persisted.PersistedOwner;
import com.wynntils.core.persisted.config.ConfigProfile;
import java.lang.reflect.Type;
import java.util.Map;

public record PersistedMetadata<T>(
        PersistedOwner owner,
        String fieldName,
        Type valueType,
        T defaultValue,
        Map<ConfigProfile, T> profileDefaultValues,
        String i18nKeyOverride,
        boolean allowNull,
        String jsonName) {}
