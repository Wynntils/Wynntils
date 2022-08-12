/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.functions;

public interface EnableableFunction<T> extends Function<T> {
    default void init() {}

    /**
     * Called on enabling of Function
     *
     * <p>Return false to cancel enabling, return true to continue.
     */
    default boolean onEnable() {
        return true;
    }

    /** Called on disabling of Function */
    default void onDisable() {}
}
