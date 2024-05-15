/*
 * Copyright © Wynntils 2023-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.character.event;

import com.wynntils.utils.mc.type.Location;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class CharacterDeathEvent extends Event {
    private final Location location;

    public CharacterDeathEvent(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}
