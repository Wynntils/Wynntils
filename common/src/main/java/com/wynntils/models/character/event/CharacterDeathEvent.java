/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.event;

import com.wynntils.utils.mc.type.Location;
import net.neoforged.bus.api.Event;

public class CharacterDeathEvent extends Event {
    private final Location location;
    private final boolean hardcoreDeath;

    public CharacterDeathEvent(Location location, boolean hardcoreDeath) {
        this.location = location;
        this.hardcoreDeath = hardcoreDeath;
    }

    public Location getLocation() {
        return location;
    }

    public boolean getHardcoreDeath() {
        return hardcoreDeath;
    }
}
