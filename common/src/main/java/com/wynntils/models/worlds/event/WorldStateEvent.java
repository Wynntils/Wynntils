/*
 * Copyright © Wynntils 2021-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds.event;

import com.wynntils.core.events.BaseEvent;
import com.wynntils.models.worlds.type.WorldState;

public class WorldStateEvent extends BaseEvent {
    private final WorldState newState;
    private final WorldState oldState;
    private final String worldName;
    private final boolean isFirstJoinWorld;

    public WorldStateEvent(WorldState newState, WorldState oldState, String worldName, boolean isFirstJoinWorld) {
        this.newState = newState;
        this.oldState = oldState;
        this.worldName = worldName;
        this.isFirstJoinWorld = isFirstJoinWorld;
    }

    public WorldState getNewState() {
        return newState;
    }

    public WorldState getOldState() {
        return oldState;
    }

    public String getWorldName() {
        return worldName;
    }

    public boolean isFirstJoinWorld() {
        return isFirstJoinWorld;
    }
}
