/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.statuseffects;

public abstract class StatusTimer {
    private final String name; // The name of the consumable (also used to identify it)

    protected StatusTimer(String name) {
        this.name = name;
    }

    /**
     * @return The name of the consumable
     */
    public String getName() {
        return name;
    }

    public abstract String asString();
}
