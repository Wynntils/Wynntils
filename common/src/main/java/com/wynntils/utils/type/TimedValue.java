/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.type;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

public class TimedValue<T> {
    private T value = null;
    private long creation;
    private final long duration;

    public TimedValue(long duration, TimeUnit unit, T initial) {
        this.value = initial;
        this.creation = System.currentTimeMillis();
        this.duration = unit.toMillis(duration);
    }

    public TimedValue(long duration, TimeUnit unit) {
        this.creation = System.currentTimeMillis();
        this.duration = unit.toMillis(duration);
    }

    public T get() {
        if (value == null || isExpired()) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public boolean matches(T value) {
        return !isExpired() && this.value.equals(value);
    }

    public void set(T value) {
        this.value = value;
        this.creation = System.currentTimeMillis();
    }

    public void reset() {
        this.value = null;
    }

    public boolean isExpired() {
        return value == null || System.currentTimeMillis() >= creation + duration;
    }
}
