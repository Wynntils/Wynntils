/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.worlds.event;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class CutsceneStartedEvent extends Event implements ICancellableEvent {
    private final boolean groupCutscene;

    public CutsceneStartedEvent(boolean groupCutscene) {
        this.groupCutscene = groupCutscene;
    }

    public boolean isGroupCutscene() {
        return groupCutscene;
    }
}
