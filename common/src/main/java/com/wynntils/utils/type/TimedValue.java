/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.type;

import java.util.concurrent.TimeUnit;

public class TimedValue<T> {
    private final T value;
    private final long creation;
    private final long duration;

    public TimedValue(T value, long duration, TimeUnit unit) {
        this.value = value;
        this.creation = System.currentTimeMillis();
        this.duration = unit.toMillis(duration);
    }

    public T get() {
        return value;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= creation + duration;
    }
}
