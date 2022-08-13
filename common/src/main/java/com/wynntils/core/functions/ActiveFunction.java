/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.functions;

public abstract class ActiveFunction<T> extends Function<T> {
    protected long lastUpdated;

    protected ActiveFunction() {
        markUpdated();
    }

    public void init() {}

    /**
     * Called on enabling of Function
     *
     * <p>Return false to cancel enabling, return true to continue.
     */
    public boolean onEnable() {
        return true;
    }

    /** Called on disabling of Function */
    public void onDisable() {}

    /**
     * Return the time the value was last updated, as given by System.currentTimeMillis().
     */
    public long lastUpdateTime() {
        return lastUpdated;
    }

    /**
     * Mark this value as updated
     */
    protected void markUpdated() {
        lastUpdated = System.currentTimeMillis();
    }
}
