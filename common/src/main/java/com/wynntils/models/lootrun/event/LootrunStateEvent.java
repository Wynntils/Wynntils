/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.lootrun.event;

import com.wynntils.models.lootrun.type.LootrunningState;
import net.neoforged.bus.api.Event;

public class LootrunStateEvent extends Event {
    private final LootrunningState newState;
    private final LootrunningState oldState;

    public LootrunStateEvent(LootrunningState newState, LootrunningState oldState) {
        this.newState = newState;
        this.oldState = oldState;
    }

    public LootrunningState getNewState() {
        return newState;
    }

    public LootrunningState getOldState() {
        return oldState;
    }
}
