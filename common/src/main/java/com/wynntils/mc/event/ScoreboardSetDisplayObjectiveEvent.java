/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.event;

import com.wynntils.core.events.BaseEvent;
import net.minecraft.world.scores.DisplaySlot;
import net.neoforged.bus.api.ICancellableEvent;

public class ScoreboardSetDisplayObjectiveEvent extends BaseEvent implements ICancellableEvent {
    private final DisplaySlot slot;
    private final String objectiveName;

    public ScoreboardSetDisplayObjectiveEvent(DisplaySlot slot, String objectiveName) {
        this.slot = slot;
        this.objectiveName = objectiveName;
    }

    public DisplaySlot getSlot() {
        return slot;
    }

    public String getObjectiveName() {
        return objectiveName;
    }
}
