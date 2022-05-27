/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

/**
 * A feature that is enabled & disabled by the user.
 */
public abstract class UserFeature extends Feature {
    protected boolean userEnabled = true;
}
