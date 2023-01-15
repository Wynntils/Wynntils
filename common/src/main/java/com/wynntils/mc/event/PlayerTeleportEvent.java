/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.WynntilsEvent;
import net.minecraft.core.Position;

/** Fires when player is teleported */
public class PlayerTeleportEvent extends WynntilsEvent {
    private final Position newPosition;

    public PlayerTeleportEvent(Position newPosition) {
        this.newPosition = newPosition;
    }

    public Position getNewPosition() {
        return newPosition;
    }
}
