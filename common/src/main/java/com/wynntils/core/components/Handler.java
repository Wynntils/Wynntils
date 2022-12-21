/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.components;

/**
 * Handlers span the bridge between Minecraft and Wynncraft. They manage a certain
 * aspect of Minecraft functionality, and with some Wynncraft knowledge, they distribute
 * the MC content to the actual Wynncraft models.
 * Handlers are created as singletons in the {@link Handlers} holding class.
 */
public abstract class Handler {
    protected Handler() {}
}
