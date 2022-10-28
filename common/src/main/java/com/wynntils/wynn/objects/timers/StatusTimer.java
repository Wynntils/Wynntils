/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.objects.timers;

public abstract class StatusTimer {
    private final String name; // The name of the consumable (also used to identify it)
    private final boolean persistent; // If the consumable should persist through death and character changes

    public StatusTimer(String name, boolean persistent) {
        this.name = name;
        this.persistent = persistent;
    }

    /**
     * @return The name of the consumable
     */
    public String getName() {
        return name;
    }

    /**
     * @return If the consumable should persist through death and character changes
     */
    public boolean isPersistent() {
        return persistent;
    }

    public abstract String asString();
}
