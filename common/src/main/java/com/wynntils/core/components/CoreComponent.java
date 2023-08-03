/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.components;

import com.google.common.base.CaseFormat;
import com.wynntils.core.persisted.storage.Storageable;
import java.util.Locale;

public abstract class CoreComponent implements Storageable {
    @Override
    public String getStorageJsonName() {
        String name = this.getClass().getSimpleName().replace(getComponentType(), "");
        String nameCamelCase = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
        return getComponentType().toLowerCase(Locale.ROOT) + "." + nameCamelCase;
    }

    protected abstract String getComponentType();
}
