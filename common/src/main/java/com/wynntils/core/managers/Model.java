/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.managers;

/**
 * Models are managers that are dependencies to features / functions.
 * They are lazy loaded and only enabled when they are a dependency to an enabled feature / function.
 * <p>The init and disable methods work like {@link Manager}'s.
 */
public abstract class Model {}
