/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import net.minecraft.core.Position;

/** Fires when player is teleported */
public final class PlayerTeleportEvent extends BaseEvent {
    private final Position newPosition;

    public PlayerTeleportEvent(Position newPosition) {
        this.newPosition = newPosition;
    }

    public Position getNewPosition() {
        return newPosition;
    }
}
