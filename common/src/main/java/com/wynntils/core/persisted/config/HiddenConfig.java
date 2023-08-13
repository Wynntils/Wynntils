/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.persisted.config;

public class HiddenConfig<T> extends Config<T> {
    public HiddenConfig(T value) {
        super(value);
    }

    @Override
    public boolean isVisible() {
        return false;
    }
}
