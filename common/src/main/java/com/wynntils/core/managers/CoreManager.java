/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.managers;

import java.util.List;

/**
 * CoreManagers are managers that are always loaded during mod initialization.
 * Currently, core manager load order is defined in {@link ManagerRegistry#init}.
 * <p>The init and disable methods work like {@link Manager}'s.
 */
public abstract class CoreManager extends Manager {
    protected CoreManager(List<CoreManager> dependencies) {
        init();
    }

    protected abstract void init();
}
