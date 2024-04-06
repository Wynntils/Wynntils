/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.type;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

public final class ThrottledSupplier<T> implements Supplier<T> {
    private final Supplier<T> method;
    private final Duration cooldown;
    private T current;
    private Instant lastExecutionTime = Instant.EPOCH;

    public ThrottledSupplier(Supplier<T> method, Duration cooldown) {
        this.method = method;
        this.cooldown = cooldown;
    }

    @Override
    public T get() {
        Duration difference = Duration.between(lastExecutionTime, Instant.now());
        if (difference.minus(cooldown).isNegative()) return current;
        lastExecutionTime = Instant.now();
        current = method.get();
        return current;
    }
}
