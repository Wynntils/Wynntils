/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.type;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class TimedValue<T> {
    private Optional<T> value = Optional.empty();
    private long creation;
    private final long duration;

    public TimedValue(long duration, TimeUnit unit, T initial) {
        this.value = Optional.ofNullable(initial);
        this.creation = System.currentTimeMillis();
        this.duration = unit.toMillis(duration);
    }

    public TimedValue(long duration, TimeUnit unit) {
        this.creation = System.currentTimeMillis();
        this.duration = unit.toMillis(duration);
    }

    public T get() throws ValueExpiredException {
        if (value.isEmpty() || isExpired()) {
            throw new ValueExpiredException();
        }
        return value.get();
    }

    public void set(T value) {
        this.value = Optional.ofNullable(value);
        this.creation = System.currentTimeMillis();
    }

    public void reset() {
        this.value = Optional.empty();
    }

    public boolean isExpired() {
        return value.isEmpty() || System.currentTimeMillis() >= creation + duration;
    }

    public static final class ValueExpiredException extends Exception {}
}
