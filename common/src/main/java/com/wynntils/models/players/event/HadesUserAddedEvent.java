/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.players.event;

import com.wynntils.models.players.hades.objects.HadesUser;
import net.minecraftforge.eventbus.api.Event;

// Fired when a new user is added to the HadesModel in our current world
public class HadesUserAddedEvent extends Event {
    private final HadesUser hadesUser;

    public HadesUserAddedEvent(HadesUser hadesUser) {
        this.hadesUser = hadesUser;
    }

    public HadesUser getHadesUser() {
        return hadesUser;
    }
}
