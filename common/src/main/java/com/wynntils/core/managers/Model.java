/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.managers;

/**
 * Models are like managers that can be dependent upon by features / functions.
 * They are lazy loaded and only enabled when they are a dependency to an enabled feature / function.
 *
 * Models can have two methods:
 * <p>
 * init: Called when manager is enabled
 * <p>
 * disable: Called when manager is disabled
 * <p>
 * Models are automatically registered to event bus when they are activated
 */
public abstract class Model {
    public void init() {}

    public void disable() {}
}
