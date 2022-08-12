/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.functions;

public abstract class EnableableFunction<T> extends Function<T> {
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
}
