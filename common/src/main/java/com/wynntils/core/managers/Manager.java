/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.managers;

import java.util.List;

/**
 * Managers constitute the core functionality of Wynntils. They are created
 * as singletons in the {@link Managers} holding class.
 */
public abstract class Manager {
    protected Manager(List<Manager> dependencies) {
        // dependencies are technically not used, but only required
        // as a reminder for implementers to be wary about dependencies
    }
}
