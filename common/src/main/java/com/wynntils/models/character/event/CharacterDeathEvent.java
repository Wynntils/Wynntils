/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.utils.mc.type.Location;

public class CharacterDeathEvent extends BaseEvent {
    private final Location location;

    public CharacterDeathEvent(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}
