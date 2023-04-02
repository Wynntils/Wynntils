/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.config;

public class Config<T> {
    private T value;

    public Config(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void updateConfig(T value) {
        this.value = value;
    }
}
