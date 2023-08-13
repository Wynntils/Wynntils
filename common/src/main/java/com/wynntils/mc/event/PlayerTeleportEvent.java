/*
 * Copyright © Wynntils 2021-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import net.minecraft.core.Position;
import net.minecraftforge.eventbus.api.Event;

/** Fires when player is teleported */
public class PlayerTeleportEvent extends Event {
    private final Position newPosition;

    public PlayerTeleportEvent(Position newPosition) {
        this.newPosition = newPosition;
    }

    public Position getNewPosition() {
        return newPosition;
    }
}
