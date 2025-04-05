/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.custommodel;

import com.wynntils.core.components.Services;
import java.util.Optional;

public final class ModelSupplier {
    private final String key;

    private ModelSupplier(String key) {
        this.key = key;
    }

    public static ModelSupplier forKey(String key) {
        return new ModelSupplier(key);
    }

    public Optional<Float> get() {
        return Services.CustomModel.getCustomModelData(key);
    }
}
